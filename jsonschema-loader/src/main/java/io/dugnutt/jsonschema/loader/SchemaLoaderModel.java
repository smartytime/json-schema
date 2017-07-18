package io.dugnutt.jsonschema.loader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import io.dugnutt.jsonschema.loader.reference.ReferenceScopeResolver;
import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonPointerPath;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Wither;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.loader.LoadingUtils.castTo;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ID;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TYPE;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NULL;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;

/**
 * Contains the
 */
@Wither
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaLoaderModel {

    public static final Set<JsonSchemaKeyword> COMBINED_SCHEMA_KEYWORDS = Sets.newHashSet(ALL_OF, ANY_OF, ONE_OF);

    /**
     * The id of the currently loading schema
     */
    @NotNull
    protected final URI id;

    /**
     * The path to the current schema within a parent document.
     */
    @NotNull
    @NonNull
    protected final JsonPointerPath currentJsonPath;

    /**
     * The parent json document (when loading a subschema)
     */
    @NonNull
    protected final FluentJsonObject rootSchemaJson;

    /**
     * The path to the current schema being loaded
     */
    @NotNull
    @NonNull
    protected final FluentJsonObject schemaJson;

    /**
     * The resolution scope for the currently loading schema
     */
    @NotNull
    protected final URI resolutionScope;

    // SchemaLoader.SchemaLoaderBuilder initChildLoader() {
    //     return SchemaLoader.builder()
    //             .resolutionScope(id)
    //             .schemaJson(schemaJson)
    //             .rootSchemaJson(rootSchemaJson)
    //             .pointerSchemas(pointerSchemas)
    //             .httpClient(httpClient)
    //             .pointerToCurrentObj(currentJsonPath);
    // }
    //
    // public LoadingState childFor(String key) {
    //     return new LoadingState(httpClient, pointerSchemas, rootSchemaJson, schemaJson, id, currentJsonPath
    //             .child(key), provider);
    // }
    //
    // public LoadingState childFor(int arrayIndex) {
    //     return new LoadingState(httpClient, pointerSchemas, rootSchemaJson, schemaJson, id, currentJsonPath
    //             .child(arrayIndex), provider);
    // }
    //
    // public LoadingState childForId(Object idAttr) {
    //     URI childId = idAttr == null || !(idAttr instanceof String)
    //             ? this.id
    //             : ReferenceResolver.resolve(this.id, (String) idAttr);
    //     return new LoadingState(initChildLoader().resolutionScope(childId));
    // }
    //
    // String currentPathUri() {
    //     return currentJsonPath.toURIFragment();
    // }

    @SneakyThrows
    public static SchemaLoaderModel createModelFor(JsonObject rootSchema) {
        final String id;
        if (rootSchema.containsKey(ID.key())) {
            id = rootSchema.getString(ID.key());
        } else {
            id = "#";
        }
        return createModelFor(rootSchema, URI.create(id), new JsonPointerPath(JsonPath.rootPath()));
    }

    @SneakyThrows
    public static SchemaLoaderModel createModelFor(JsonObject rootSchema, URI id, JsonPointerPath path) {
        FluentJsonObject schemaObject = new FluentJsonObject(rootSchema, path);
        return new SchemaLoaderModel(id, path, schemaObject, schemaObject, URI.create(path.toURIFragment()));
    }

    public Optional<SchemaLoaderModel> childModel(JsonSchemaKeyword childKey) {
        return Optional.ofNullable(childModel(childKey.key()));
    }

    public SchemaLoaderModel childModel(JsonSchemaKeyword arrayKey, int idx) {
        JsonArray jsonArray = schemaJson.getJsonArray(arrayKey.key());
        JsonValue indexValue = jsonArray.get(idx);
        return internalChildModel(arrayKey, idx, indexValue);
    }

    public SchemaLoaderModel childModel(JsonSchemaKeyword keyWord, String childKey, JsonValue valueAtKey) {
        return internalChildModel(keyWord, childKey, valueAtKey);
    }

    public SchemaException createSchemaException(String message) {
        return new SchemaException(currentJsonPath.toURIFragment(), message);
    }

    public JsonSchemaType getExplicitType() {
        try {
            return JsonSchemaType.fromString(schemaJson.getString(TYPE));
        } catch (IllegalArgumentException e) {
            throw new UnexpectedValueException(currentJsonPath, schemaJson.get(TYPE.key()), NUMBER, TRUE, FALSE, ARRAY, STRING, NULL, OBJECT);
        }
    }

    public String getPropertyName() {
        return currentJsonPath.jsonPath().getLastPath().orElseThrow(() -> new SchemaException(currentJsonPath.toURIFragment(), "Invalid path"));
    }

    public Set<JsonSchemaType> getTypeArray() {
        return schemaJson.getJsonArray(TYPE.key())
                .getValuesAs(castTo(JsonString.class, currentJsonPath)) // This ensures that any classCast exceptions get bubbled correctly
                .stream()
                .map(JsonString::getString)
                .map(JsonSchemaType::fromString)
                .collect(Collectors.toSet());
    }

    public boolean has(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");
        return schemaJson.has(property);
    }

    public boolean hasExplicitTypeArray() {
        return schemaJson.has(TYPE) && schemaJson.get(TYPE.key()).getValueType() == ARRAY;
    }

    public boolean hasExplicitTypeValue() {
        return schemaJson.has(TYPE) && schemaJson.get(TYPE.key()).getValueType() != ARRAY;
    }

    public boolean isCombinedSchema() {
        for (JsonSchemaKeyword combinedSchemaKeyword : COMBINED_SCHEMA_KEYWORDS) {
            if (schemaJson.findByKey(combinedSchemaKeyword).isPresent()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return schemaJson.isEmpty();
    }

    public boolean isEnumSchema() {
        return schemaJson.has(ENUM);
    }

    public boolean isNotSchema() {
        return schemaJson.has(NOT);
    }

    public boolean isRefSchema() {
        return schemaJson.has($REF);
    }

    public boolean isSchemaOf(JsonSchemaType type) {
        return Arrays.stream(JsonSchemaKeyword.values())
                .filter(p -> p.appliesToType(type))
                .map(JsonSchemaKeyword::key)
                .anyMatch(schemaJson::containsKey);
    }

    public Stream<SchemaLoaderModel> streamChildSchemaModels(JsonSchemaKeyword schemaProperty, JsonValue.ValueType... validTypes) {
        checkNotNull(schemaProperty, "array must not be null");

        if (!schemaJson.containsKey(schemaProperty.key())) {
            return Stream.empty();
        }

        JsonValue jsonValue = schemaJson.get(schemaProperty.key());

        if (!JsonUtils.isOneOf(jsonValue, validTypes)) {
            throw unexpectedValueException(schemaProperty, jsonValue, validTypes);
        }

        if (jsonValue.getValueType() == OBJECT) {
            return streamChildSchemasByKey(schemaProperty, jsonValue.asJsonObject());
        } else if (jsonValue.getValueType() == ARRAY) {
            return streamChildSchemaModelsForArray(schemaProperty, jsonValue.asJsonArray());
        } else {
            throw unexpectedValueException(schemaProperty, jsonValue, OBJECT, ARRAY);
        }
    }

    public boolean isPropertyType(JsonSchemaKeyword property, JsonValue.ValueType valueType) {
        final JsonValue jsonValue = schemaJson.get(property.key());
        if (jsonValue != null && jsonValue.getValueType() == valueType) {
            return true;
        }
        return false;
    }

    private Stream<SchemaLoaderModel> streamChildSchemaModelsForArray(JsonSchemaKeyword schemaProperty, JsonArray toIterate) {
        return IntStream.range(0, toIterate.size())
                .mapToObj(idx -> {
                    final JsonValue jsonValue = toIterate.get(idx);
                    return this.internalChildModel(schemaProperty, idx, jsonValue);
                });
    }

    public SchemaException unexpectedValueException(JsonSchemaKeyword keyword, int idx, JsonValue errorSource, JsonValue.ValueType... values) {
        return new UnexpectedValueException(currentJsonPath.child(keyword.key()).child(idx), errorSource, values);
    }

    public SchemaException unexpectedValueException(String propertyName, JsonValue errorSource, JsonValue.ValueType... values) {
        return new UnexpectedValueException(currentJsonPath.child(propertyName), errorSource, values);
    }

    public SchemaException unexpectedValueException(JsonSchemaKeyword keyword, JsonValue errorSource, JsonValue.ValueType... values) {
        return new UnexpectedValueException(currentJsonPath, errorSource, values);
    }

    @VisibleForTesting
    Optional<SchemaLoaderModel> childModelIfObject(JsonSchemaKeyword keyword) {
        final String keywordVal = keyword.key();
        return schemaJson.findIfObject(keywordVal)
                .map(childObject -> {
                    final JsonPointerPath childPath = this.currentJsonPath.child(keywordVal);

                    final FluentJsonObject childJsonWrapper = new FluentJsonObject(childObject, childPath);

                    final URI childResolutionScope;
                    if (childJsonWrapper.has(ID)) {
                        childResolutionScope = ReferenceScopeResolver.resolveScope(id, childJsonWrapper.getString(ID));
                    } else {
                        childResolutionScope = this.resolutionScope;
                    }
                    return this.withCurrentJsonPath(childPath)
                            .withSchemaJson(childJsonWrapper)
                            .withResolutionScope(childResolutionScope);
                });
    }

    @VisibleForTesting
    @Nullable
    SchemaLoaderModel childModel(String childKey) {
        return schemaJson.findObject(childKey)
                .map(childObject -> {
                    final JsonPointerPath childPath = this.currentJsonPath.child(childKey);

                    final FluentJsonObject childJsonWrapper = new FluentJsonObject(childObject, childPath);

                    final URI childResolutionScope;
                    if (childJsonWrapper.has(ID)) {
                        childResolutionScope = ReferenceScopeResolver.resolveScope(id, childJsonWrapper.getString(ID));
                    } else {
                        childResolutionScope = this.resolutionScope;
                    }
                    return this.withCurrentJsonPath(childPath)
                            .withSchemaJson(childJsonWrapper)
                            .withResolutionScope(childResolutionScope);
                }).orElse(null);
    }

    private SchemaLoaderModel internalChildModel(JsonSchemaKeyword keyWord, Object childKey, JsonValue valueAtKey) {
        checkNotNull(keyWord, "keyWord must not be null");
        checkNotNull(childKey, "childKey must not be null");
        checkNotNull(valueAtKey, "valueAtKey must not be null");

        final JsonPointerPath childPath;
        if (childKey instanceof Integer) {
            childPath = currentJsonPath.child(keyWord.key()).child((int) childKey);
        } else if (childKey instanceof String) {
            childPath = currentJsonPath.child(keyWord.key()).child(childKey.toString());
        } else {
            throw unexpectedValueException(String.valueOf(childKey), valueAtKey, STRING, NUMBER);
        }

        if (valueAtKey.getValueType() != OBJECT) {
            throw new UnexpectedValueException(currentJsonPath, valueAtKey, OBJECT);
        }

        final FluentJsonObject childJsonObject = new FluentJsonObject(valueAtKey.asJsonObject(), childPath);

        final URI childResolutionScope;
        if (childJsonObject.has(ID)) {
            childResolutionScope = ReferenceScopeResolver.resolveScope(id, childJsonObject.getString(ID));
        } else {
            childResolutionScope = this.resolutionScope;
        }

        return this.withCurrentJsonPath(childPath)
                .withSchemaJson(childJsonObject)
                .withResolutionScope(childResolutionScope);
    }

    private Stream<SchemaLoaderModel> streamChildSchemasByKey(JsonSchemaKeyword schemaProperty, JsonObject toIterate) {
        return toIterate.entrySet().stream()
                .map(entry -> this.childModel(schemaProperty, entry.getKey(), entry.getValue()));
    }
}
