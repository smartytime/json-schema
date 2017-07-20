package io.dugnutt.jsonschema.loader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
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
@Builder(toBuilder = true)
public class SchemaLoadingContext {

    public static final Set<JsonSchemaKeyword> COMBINED_SCHEMA_KEYWORDS = Sets.newHashSet(ALL_OF, ANY_OF, ONE_OF);
    /**
     * The parent json document (when loading a subschema)
     */
    @NonNull
    protected final JsonObject rootSchemaJson;

    /**
     * The path to the current schema being loaded
     */
    @NotNull
    @NonNull
    protected final PathAwareJsonValue schemaJson;
    private final SchemaLocation location;

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
    public static SchemaLoadingContext createModelFor(JsonObject rootSchema) {
        final SchemaLocation rootSchemaLocation;
        if (rootSchema.containsKey($ID.key())) {
            String $id = rootSchema.getString($ID.key());
            rootSchemaLocation = SchemaLocation.schemaLocation($id);
        } else {
            rootSchemaLocation = SchemaLocation.schemaLocation();
        }

        PathAwareJsonValue schemaJson = new PathAwareJsonValue(rootSchema, rootSchemaLocation.getJsonPath());
        return builder()
                .location(rootSchemaLocation)
                .pathedSchemaJson(schemaJson)
                .rootSchemaJson(schemaJson)
                .build();
    }

    public Optional<SchemaLoadingContext> childModel(JsonSchemaKeyword childKey) {
        return Optional.ofNullable(childModel(childKey.key()));
    }

    public SchemaLoadingContext childModel(JsonSchemaKeyword arrayKey, int idx) {
        JsonArray jsonArray = schemaJson.getJsonArray(arrayKey.key());
        JsonValue indexValue = jsonArray.get(idx);
        return internalChildModel(arrayKey, idx, indexValue);
    }

    public SchemaLoadingContext childModel(JsonSchemaKeyword keyWord, String childKey, JsonValue valueAtKey) {
        return internalChildModel(keyWord, childKey, valueAtKey);
    }

    public SchemaException createSchemaException(String message) {
        return new SchemaException(location.getJsonPointerFragment(), message);
    }

    public JsonSchemaType getExplicitType() {
        try {
            return JsonSchemaType.fromString(schemaJson.getString(TYPE));
        } catch (IllegalArgumentException e) {
            throw new UnexpectedValueException(location, schemaJson.get(TYPE.key()), NUMBER, TRUE, FALSE, ARRAY, STRING, NULL, OBJECT);
        }
    }

    public SchemaLocation getLocation() {
        return location;
    }

    public String getPropertyName() {
        return location.getJsonPath().getLastPath().orElseThrow(() -> new SchemaException(location.getJsonPointerFragment(), "Invalid path"));
    }

    public Set<JsonSchemaType> getTypeArray() {
        return schemaJson.getJsonArray(TYPE.key())
                .getValuesAs(castTo(JsonString.class, location.withChildPath(TYPE.key()).getJsonPointerFragment())) // This ensures that any classCast exceptions get bubbled correctly
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

    public boolean isPropertyType(JsonSchemaKeyword property, JsonValue.ValueType valueType) {
        final JsonValue jsonValue = schemaJson.get(property.key());
        if (jsonValue != null && jsonValue.getValueType() == valueType) {
            return true;
        }
        return false;
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

    public Stream<SchemaLoadingContext> streamChildSchemaModels(JsonSchemaKeyword schemaProperty, JsonValue.ValueType... validTypes) {
        checkNotNull(schemaProperty, "array must not be null");

        if (!schemaJson.containsKey(schemaProperty.key())) {
            return Stream.empty();
        }

        PathAwareJsonValue jsonValue = schemaJson.getPathAware(schemaProperty.key());

        if (validTypes.length > 0 && !jsonValue.is(validTypes)) {
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

    public SchemaException unexpectedValueException(JsonSchemaKeyword keyword, int idx, JsonValue errorSource, JsonValue.ValueType... values) {
        SchemaLocation errorPath = location.withChildPath(keyword.key(), idx);
        return new UnexpectedValueException(errorPath, errorSource, values);
    }

    public SchemaException unexpectedValueException(String propertyName, JsonValue errorSource, JsonValue.ValueType... values) {
        return new UnexpectedValueException(location.withChildPath(propertyName), errorSource, values);
    }

    public SchemaException unexpectedValueException(JsonSchemaKeyword keyword, JsonValue errorSource, JsonValue.ValueType... values) {
        return new UnexpectedValueException(location.withChildPath(keyword), errorSource, values);
    }

    @VisibleForTesting
    Optional<SchemaLoadingContext> childModelIfObject(JsonSchemaKeyword keyword) {
        final String keywordVal = keyword.key();
        return schemaJson.findIfObject(keywordVal)
                .map(childObject -> {

                    SchemaLocation childLocation = getChildLocationWithId(childObject, keywordVal);
                    JsonPath childPath = childLocation.getJsonPath();
                    final PathAwareJsonValue awareJsonValue = new PathAwareJsonValue(childObject, childPath);

                    return this.toBuilder().location(childLocation)
                            .pathedSchemaJson(awareJsonValue)
                            .build();
                });
    }

    @VisibleForTesting
    @Nullable
    SchemaLoadingContext childModel(String childKey) {
        return schemaJson.findObject(childKey)
                .map(childObject -> {
                    final SchemaLocation childPath = getChildLocationWithId(childObject, childKey);

                    JsonPath childPointer = childPath.getJsonPath();
                    final PathAwareJsonValue pathAwareValue = new PathAwareJsonValue(childObject, childPointer);

                    return this.toBuilder().location(childPath)
                            .pathedSchemaJson(pathAwareValue)
                            .build();
                }).orElse(null);
    }

    private SchemaLocation getChildLocationWithId(JsonObject childObject, String... childKeys) {
        final SchemaLocation childLocation;
        if (childObject.containsKey($ID.key())) {
            String id = childObject.getString($ID.key());
            childLocation = this.location.withChildPath(URI.create(id), childKeys);
        } else {
            childLocation = this.location.withChildPath(childKeys);
        }
        return childLocation;
    }

    private Stream<SchemaLoadingContext> streamChildSchemaModelsForArray(JsonSchemaKeyword schemaProperty, JsonArray toIterate) {
        return IntStream.range(0, toIterate.size())
                .mapToObj(idx -> {
                    final JsonValue jsonValue = toIterate.get(idx);
                    return this.internalChildModel(schemaProperty, idx, jsonValue);
                });
    }

    private SchemaLoadingContext internalChildModel(JsonSchemaKeyword keyWord, Object childKey, JsonValue valueAtKey) {
        checkNotNull(keyWord, "keyWord must not be null");
        checkNotNull(childKey, "childKey must not be null");
        checkNotNull(valueAtKey, "valueAtKey must not be null");

        if (valueAtKey.getValueType() != OBJECT) {
            throw new UnexpectedValueException(this.location.withChildPath(keyWord.key(), childKey.toString()), valueAtKey, OBJECT);
        }

        SchemaLocation childPath = getChildLocationWithId(valueAtKey.asJsonObject(), keyWord.key(), String.valueOf(childKey));

        // final SchemaLocation childPath;
        // if (childKey instanceof Integer) {
        //     childPath = location.withChildPath(keyWord.key(), (int) childKey);
        // } else if (childKey instanceof String) {
        //     childPath = location.withChildPath(keyWord.key(), childKey.toString());
        // } else {
        //     SchemaLocation errorLocation = location.withChildPath(keyWord.key(), childKey.toString());
        //     throw new UnexpectedValueException(errorLocation, valueAtKey, STRING, NUMBER);
        // }

        final PathAwareJsonValue childJsonObject = new PathAwareJsonValue(valueAtKey.asJsonObject(), childPath.getJsonPath());

        return this.toBuilder()
                .location(childPath)
                .pathedSchemaJson(childJsonObject)
                .build();
    }

    private Stream<SchemaLoadingContext> streamChildSchemasByKey(JsonSchemaKeyword schemaProperty, JsonObject toIterate) {
        return toIterate.entrySet().stream()
                .map(entry -> this.childModel(schemaProperty, entry.getKey(), entry.getValue()));
    }

    public static class SchemaLoadingContextBuilder {
        private JsonObject plainJson;

        public SchemaLoadingContextBuilder location(SchemaLocation location) {
            if (plainJson != null) {
                this.pathedSchemaJson(new PathAwareJsonValue(plainJson, location.getJsonPath()));
            }
            this.location = location;
            return this;
        }

        public SchemaLoadingContextBuilder pathedSchemaJson(PathAwareJsonValue schemaJson) {
            if (this.rootSchemaJson == null) {
                this.rootSchemaJson = schemaJson;
            }
            this.schemaJson = schemaJson;
            return this;
        }

        public SchemaLoadingContextBuilder schemaJson(JsonObject jsonObject) {
            if (!(jsonObject instanceof PathAwareJsonValue)) {
                this.plainJson = storeIfNoLocationPresent(jsonObject);
            } else {
                this.schemaJson = (PathAwareJsonValue) jsonObject;
            }

            return this;
        }

        private JsonObject storeIfNoLocationPresent(JsonObject jsonObject) {
            if (this.location == null) {
                return jsonObject;
            } else {
                this.pathedSchemaJson(new PathAwareJsonValue(jsonObject, this.location.getJsonPath()));
                return null;
            }
        }
    }
}
