package org.everit.jsonschema.loader;

import org.everit.jsonschema.api.ArraySchema;
import org.everit.jsonschema.api.BooleanSchema;
import org.everit.jsonschema.api.CombinedSchema;
import org.everit.jsonschema.api.EmptySchema;
import org.everit.jsonschema.api.EnumSchema;
import org.everit.jsonschema.api.JsonPath;
import org.everit.jsonschema.api.JsonPointerPath;
import org.everit.jsonschema.api.JsonSchemaProperty;
import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.NotSchema;
import org.everit.jsonschema.api.NullSchema;
import org.everit.jsonschema.api.NumberSchema;
import org.everit.jsonschema.api.ObjectSchema;
import org.everit.jsonschema.api.ReferenceSchema;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.api.SchemaException;
import org.everit.jsonschema.api.UnexpectedValueException;
import org.everit.jsonschema.loader.exceptions.JsonException;
import org.everit.jsonschema.loader.internal.DefaultSchemaClient;
import org.everit.jsonschema.loader.internal.ReferenceResolver;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.STRING;
import static org.everit.jsonschema.api.JsonSchemaProperty.$REF;
import static org.everit.jsonschema.api.JsonSchemaProperty.DESCRIPTION;
import static org.everit.jsonschema.api.JsonSchemaProperty.ENUM;
import static org.everit.jsonschema.api.JsonSchemaProperty.EXCLUSIVE_MAXIMUM;
import static org.everit.jsonschema.api.JsonSchemaProperty.EXCLUSIVE_MINIMUM;
import static org.everit.jsonschema.api.JsonSchemaProperty.ID;
import static org.everit.jsonschema.api.JsonSchemaProperty.MAXIMUM;
import static org.everit.jsonschema.api.JsonSchemaProperty.MINIMUM;
import static org.everit.jsonschema.api.JsonSchemaProperty.MULTIPLE_OF;
import static org.everit.jsonschema.api.JsonSchemaProperty.NOT;
import static org.everit.jsonschema.api.JsonSchemaProperty.TITLE;
import static org.everit.jsonschema.api.JsonSchemaProperty.TYPE;

/**
 * Loads a JSON schema's JSON representation into schema validator instances.
 */
class SchemaLoader {

    private final LoadingState loadingState;
    private final JsonProvider provider;

    /**
     * Constructor.
     *
     * @param builder the builder containing the properties. Only {@link SchemaLoaderBuilder#id} is
     *                nullable.
     * @throws NullPointerException if any of the builder properties except {@link SchemaLoaderBuilder#id id} is
     *                              {@code null}.
     */
    SchemaLoader(final SchemaLoaderBuilder builder) {
        URI id = builder.id;
        this.provider = checkNotNull(builder.provider);
        if (id == null && builder.schemaJson.containsKey(ID.key())) {
            try {
                id = new URI(builder.schemaJson.getString(ID.key()));
            } catch (JsonException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        this.loadingState = new LoadingState(builder.httpClient,
                builder.pointerSchemas,
                builder.rootSchemaJson == null ? builder.schemaJson : builder.rootSchemaJson,
                builder.schemaJson,
                id,
                builder.pointerToCurrentObj, provider);
    }

    /**
     * Populates a {@code Schema.Builder} instance from the {@code schemaJson} schema definition.
     *
     * @return the builder which already contains the validation criteria of the schema, therefore
     * {@link Schema.Builder#build()} can be immediately used to acquire the {@link Schema}
     * instance to be used for validation
     */
    public Schema.Builder<?> load() {
        Schema.Builder builder;
        if (loadingState.schemaJson.has(ENUM)) {
            builder = buildEnumSchema();
        } else {
            builder = new CombinedSchemaLoader(loadingState, this).load()
                    .orElseGet(() -> {
                        if (!loadingState.schemaJson.has(TYPE) || loadingState.schemaJson.has($REF)) {
                            return buildSchemaWithoutExplicitType();
                        } else {
                            return loadForType(loadingState.schemaJson.get(TYPE.key()));
                        }
                    });
        }

        loadingState.schemaJson.findString(ID).map(JsonString::getString).ifPresent(builder::id);
        loadingState.schemaJson.findString(TITLE).map(JsonString::getString).ifPresent(builder::title);
        loadingState.schemaJson.findString(DESCRIPTION).map(JsonString::getString).ifPresent(builder::description);
        builder.schemaLocation(loadingState.pointerToCurrentObj.toURIFragment());
        return builder;
    }

    static SchemaLoaderBuilder builder() {
        return new SchemaLoaderBuilder();
    }

    Schema.Builder loadForType(JsonValue element) {
        if (element.getValueType() == ARRAY) {
            return buildAnyOfSchemaForMultipleTypes();
        } else if (element.getValueType() == STRING) {
            return loadForExplicitType(((JsonString) element).getString());
        } else {
            throw new UnexpectedValueException(element, ARRAY, STRING);
        }
    }

    Schema.Builder<?> loadChild(JsonObject json) {
        SchemaJsonWrapper childJson = new SchemaJsonWrapper(json);

        SchemaLoaderBuilder childBuilder = loadingState.initChildLoader()
                .schemaJson(childJson)
                .pointerToCurrentObj(loadingState.pointerToCurrentObj);
        if (childJson.has(ID)) {
            URI childURL = ReferenceResolver.resolve(this.loadingState.id, childJson.expectString(ID).getString());
            childBuilder.resolutionScope(childURL);
        }
        return childBuilder.build().load();
    }

    Schema.Builder<?> sniffSchemaByProps() {
        if (schemaIsOfType(JsonSchemaType.Array)) {
            return buildArraySchema().requiresArray(false);
        } else if (schemaIsOfType(JsonSchemaType.Object)) {
            return buildObjectSchema().requiresObject(false);
        } else if (schemaIsOfType(JsonSchemaType.Number)) {
            return buildNumberSchema().requiresNumber(false);
        } else if (schemaIsOfType(JsonSchemaType.String)) {
            return new StringSchemaLoader(loadingState).load().requiresString(false);
        }
        return null;
    }

    private CombinedSchema.Builder buildAnyOfSchemaForMultipleTypes() {
        JsonArray subtypeJsons = loadingState.schemaJson.expectArray(TYPE);
        List<Schema> subSchemas = subtypeJsons.getValuesAs(JsonString.class).stream()
                .map(JsonString::getString)
                .map(this::loadForExplicitType)
                .map(Schema.Builder::build)
                .collect(Collectors.toList());
        return CombinedSchema.anyOf(subSchemas);
    }

    private EnumSchema.Builder buildEnumSchema() {
        return EnumSchema.builder().possibleValues(loadingState.schemaJson.expectArray(ENUM));
    }

    private NotSchema.Builder buildNotSchema() {
        JsonObject notSchema = loadingState.schemaJson.expectObject(NOT);
        SchemaJsonWrapper childSchema = new SchemaJsonWrapper(notSchema);
        Schema mustNotMatch = loadChild(childSchema).build();
        return NotSchema.builder().mustNotMatch(mustNotMatch);
    }

    private Schema.Builder<?> buildSchemaWithoutExplicitType() {
        if (loadingState.schemaJson.isEmpty()) {
            return EmptySchema.builder();
        }
        if (loadingState.schemaJson.has($REF)) {
            String ref = loadingState.schemaJson.expectString($REF).getString();
            return new ReferenceLookup(loadingState).lookup(ref, loadingState.schemaJson);
        }
        Schema.Builder<?> rval = sniffSchemaByProps();
        if (rval != null) {
            return rval;
        }
        if (loadingState.schemaJson.has(NOT)) {
            return buildNotSchema();
        }
        return EmptySchema.builder();
    }

    private NumberSchema.Builder buildNumberSchema() {
        NumberSchema.Builder builder = NumberSchema.builder();
        loadingState.schemaJson.findNumber(MINIMUM).map(JsonNumber::doubleValue).ifPresent(builder::minimum);
        loadingState.schemaJson.findNumber(MAXIMUM).map(JsonNumber::doubleValue).ifPresent(builder::maximum);
        loadingState.schemaJson.findNumber(MULTIPLE_OF).map(JsonNumber::doubleValue).ifPresent(builder::multipleOf);
        loadingState.schemaJson.findBoolean(EXCLUSIVE_MINIMUM).ifPresent(builder::exclusiveMinimum);
        loadingState.schemaJson.findBoolean(EXCLUSIVE_MAXIMUM).ifPresent(builder::exclusiveMaximum);
        return builder;
    }

    private Schema.Builder<?> loadForExplicitType(final String typeString) {
        switch (typeString) {
            case "string":
                return new StringSchemaLoader(loadingState).load().requiresString(true);
            case "integer":
                return buildNumberSchema().requiresInteger(true);
            case "number":
                return buildNumberSchema().requiresNumber(true);
            case "boolean":
                return BooleanSchema.builder();
            case "null":
                return NullSchema.builder();
            case "array":
                return buildArraySchema().requiresArray(true);
            case "object":
                return buildObjectSchema().requiresObject(true);
            default:
                throw new SchemaException(String.format("unknown type: [%s]", typeString));
        }
    }

    private ObjectSchema.Builder buildObjectSchema() {
        return new ObjectSchemaLoader(loadingState, this).load();
    }

    private ArraySchema.Builder buildArraySchema() {
        return new ArraySchemaLoader(loadingState, this).load();
    }

    private boolean schemaIsOfType(JsonSchemaType type) {
        return Arrays.stream(JsonSchemaProperty.values())
                .filter(p -> p.appliesToType(type))
                .map(JsonSchemaProperty::key)
                .anyMatch(loadingState.schemaJson::containsKey);
    }

    /**
     * Builder class for {@link SchemaLoader}.
     */
    protected static class SchemaLoaderBuilder {

        SchemaClient httpClient = new DefaultSchemaClient();

        JsonObject schemaJson;

        JsonObject rootSchemaJson;

        Map<String, ReferenceSchema.Builder> pointerSchemas = new HashMap<>();

        URI id;

        JsonPointerPath pointerToCurrentObj = new JsonPointerPath(JsonPath.rootPath());

        JsonProvider provider = JsonProvider.provider();

        public SchemaLoader build() {
            return new SchemaLoader(this);
        }

        public SchemaLoaderBuilder httpClient(SchemaClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public SchemaLoaderBuilder provider(JsonProvider jsonProvider) {
            this.provider = jsonProvider;
            return this;
        }

        public SchemaLoaderBuilder resolutionScope(URI id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the initial resolution scope of the schema. {@code id} and {@code $ref} attributes
         * accuring in the schema will be resolved against this value.
         *
         * @param id the initial (absolute) URI, used as the resolution scope.
         * @return {@code this}
         */
        public SchemaLoaderBuilder resolutionScope(String id) {
            try {
                return resolutionScope(new URI(id));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public SchemaLoaderBuilder schemaJson(JsonObject schemaJson) {
            this.schemaJson = schemaJson;
            return this;
        }

        SchemaLoaderBuilder pointerSchemas(Map<String, ReferenceSchema.Builder> pointerSchemas) {
            this.pointerSchemas = pointerSchemas;
            return this;
        }

        SchemaLoaderBuilder rootSchemaJson(JsonObject rootSchemaJson) {
            this.rootSchemaJson = rootSchemaJson;
            return this;
        }

        SchemaLoaderBuilder pointerToCurrentObj(JsonPointerPath pointerToCurrentObj) {
            this.pointerToCurrentObj = requireNonNull(pointerToCurrentObj);
            return this;
        }
    }
}
