package io.dugnutt.jsonschema.loader;

import com.google.common.collect.Sets;
import io.dugnutt.jsonschema.loader.internal.ReferenceResolver;
import io.dugnutt.jsonschema.six.JsonPointerPath;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Wither;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;
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
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@Getter
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
    protected final JsonPointerPath currentJsonPath;

    /**
     * The parent json document (when loading a subschema)
     */
    protected final SchemaJsonObject rootSchemaJson;

    /**
     * The path to the current schema being loaded
     */
    @NotNull
    protected final SchemaJsonObject schemaJson;

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

    public SchemaLoaderModel childModel(JsonSchemaKeyword childKey) {
        final JsonPointerPath childPath = this.currentJsonPath.child(childKey.key());
        final JsonObject childJson = this.schemaJson.getJsonObject(childKey);
        final SchemaJsonObject childJsonWrapper = new SchemaJsonObject(childJson, childPath);

        final URI childResolutionScope;
        if (childJsonWrapper.has($ID)) {
            childResolutionScope = ReferenceResolver.resolve(id, childJsonWrapper.getString($ID));
        } else {
            childResolutionScope = this.resolutionScope;
        }

        return this.withCurrentJsonPath(childPath)
                .withSchemaJson(childJsonWrapper)
                .withResolutionScope(childResolutionScope);
    }

    /**
     * Sometimes your schema jumps a level.  This method allows you to provide nested paths, btu still
     * construct the object blah blah
     * todo:ericm Explain better
     *
     * @param childJsonValue
     * @param childNameOrIndex
     * @return
     */
    public SchemaLoaderModel childModel(JsonValue childJsonValue, JsonSchemaKeyword keyWord, String childNameOrIndex) {
        checkNotNull(childJsonValue, "childJson must not be null");
        checkNotNull(keyWord, "keyWord must not be null");
        checkNotNull(childNameOrIndex, "childNameOrIndex must not be null");

        final JsonPointerPath childPath = currentJsonPath.child(keyWord.key()).child(childNameOrIndex);

        if (childJsonValue.getValueType() != OBJECT) {
            throw new UnexpectedValueException(childPath, childJsonValue, OBJECT);
        }

        final SchemaJsonObject childJsonObject = new SchemaJsonObject(childJsonValue.asJsonObject(), childPath);

        final URI childResolutionScope;
        if (childJsonObject.has($ID)) {
            childResolutionScope = ReferenceResolver.resolve(id, childJsonObject.getString($ID));
        } else {
            childResolutionScope = this.resolutionScope;
        }

        return this.withCurrentJsonPath(childPath)
                .withSchemaJson(childJsonObject)
                .withResolutionScope(childResolutionScope);
    }

    public SchemaException createSchemaException(String message) {
        return new SchemaException(currentJsonPath.toURIFragment(), message);
    }

    public JsonSchemaType getExplicitType() {
        try {
            return JsonSchemaType.valueOf(schemaJson.getString(TYPE));
        } catch (IllegalArgumentException e) {
            throw new UnexpectedValueException(currentJsonPath, schemaJson.get(TYPE.key()), NUMBER, TRUE, FALSE, ARRAY, STRING, NULL, OBJECT);
        }
    }

    public boolean has(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");
        return schemaJson.has(property);
    }

    public boolean hasExplicitTypeArray() {
        return schemaJson.has(TYPE) && schemaJson.get(TYPE.key()).getValueType() == ARRAY;
    }

    public boolean hasExplicitTypeValue() {
        return schemaJson.has(TYPE) && schemaJson.get(TYPE.key()).getValueType() == STRING;
    }

    public boolean isCombinedSchema() {
        for (JsonSchemaKeyword combinedSchemaKeyword : COMBINED_SCHEMA_KEYWORDS) {
            if (schemaJson.find(combinedSchemaKeyword).isPresent()) {
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

    /**
     * This will produce a stream over an array, cast each item in the array, and report any error to the correct
     * index in the array.
     *
     * @param schemaProperty
     * @param expectedType
     * @param <X>
     * @return
     */
    public Stream<SchemaLoaderModel> streamArrayChildSchemas(JsonSchemaKeyword schemaProperty) {
        checkNotNull(schemaProperty, "array must not be null");

        final JsonArray array = schemaJson.getJsonArray(schemaProperty.key());
        return IntStream.range(0, array.size())
                .mapToObj(idx -> {
                    final JsonValue jsonValue = array.get(idx);
                    return this.childModel(jsonValue, schemaProperty, String.valueOf(idx));
                });
    }
}
