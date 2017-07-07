package org.everit.jsonschema.loader;

import org.everit.json.JsonApi;
import org.everit.json.JsonArray;
import org.everit.json.JsonElement;
import org.everit.json.JsonObject;
import org.everit.jsonschema.api.ArraySchema;
import org.everit.jsonschema.api.BooleanSchema;
import org.everit.jsonschema.api.CombinedSchema;
import org.everit.jsonschema.api.EmptySchema;
import org.everit.jsonschema.api.EnumSchema;
import org.everit.jsonschema.api.JsonSchemaProperty;
import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.NotSchema;
import org.everit.jsonschema.api.NullSchema;
import org.everit.jsonschema.api.NumberSchema;
import org.everit.jsonschema.api.ObjectSchema;
import org.everit.jsonschema.api.ReferenceSchema;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.api.SchemaException;
import org.everit.jsonschema.loader.exceptions.JsonException;
import org.everit.jsonschema.loader.internal.DefaultSchemaClient;
import org.everit.jsonschema.loader.internal.ReferenceResolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
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
public class SchemaLoader {

    private final LoadingState ls;

    /**
     * Constructor.
     *
     * @param builder the builder containing the properties. Only {@link SchemaLoaderBuilder#id} is
     *                nullable.
     * @throws NullPointerException if any of the builder properties except {@link SchemaLoaderBuilder#id id} is
     *                              {@code null}.
     */
    public SchemaLoader(final SchemaLoaderBuilder builder) {
        URI id = builder.id;
        if (id == null && builder.schemaJson.has(ID)) {
            try {
                id = new URI(builder.schemaJson.get(ID).asString());
            } catch (JsonException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        this.ls = new LoadingState(builder.httpClient,
                builder.pointerSchemas,
                builder.jsonApi,
                builder.rootSchemaJson == null ? builder.schemaJson : builder.rootSchemaJson,
                builder.schemaJson,
                id,
                builder.pointerToCurrentObj);
    }

    /**
     * Constructor.
     *
     * @deprecated use {@link SchemaLoader#SchemaLoader(SchemaLoaderBuilder)} instead.
     */
    @Deprecated
    SchemaLoader(final String id, final JsonObject schemaJson,
                 final JsonObject rootSchemaJson, final Map<String, ReferenceSchema.Builder> pointerSchemas,
                 final SchemaClient httpClient) {
        this(builder().schemaJson(schemaJson)
                .rootSchemaJson(rootSchemaJson)
                .resolutionScope(id)
                .httpClient(httpClient)
                .pointerSchemas(pointerSchemas));
    }

    public static SchemaLoaderBuilder builder() {
        return new SchemaLoaderBuilder();
    }

    /**
     * Loads a JSON schema to a schema validator using a {@link DefaultSchemaClient default HTTP
     * client}.
     *
     * @param schemaJson the JSON representation of the schema.
     * @return the schema validator object
     */
    public static Schema load(final String schemaJson, JsonApi jsonApi) {
        return SchemaLoader.load(schemaJson, new DefaultSchemaClient(), jsonApi);
    }

    /**
     * Creates Schema instance from its JSON representation.
     *
     * @param schemaJson the JSON representation of the schema.
     * @param httpClient the HTTP client to be used for resolving remote JSON references.
     * @return the created schema
     */
    public static Schema load(final String schemaJson, final SchemaClient httpClient,
                              final JsonApi jsonApi) {
        SchemaLoader loader = builder()
                .schemaJson(jsonApi.readJson(schemaJson))
                .jsonApi(jsonApi)
                .httpClient(httpClient)
                .build();
        return loader.load().build();
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
        if (ls.schemaJson.has(ENUM)) {
            builder = buildEnumSchema();
        } else {
            builder = new CombinedSchemaLoader(ls, this).load()
                    .orElseGet(() -> {
                        if (!ls.schemaJson.has(TYPE) || ls.schemaJson.has($REF)) {
                            return buildSchemaWithoutExplicitType();
                        } else {
                            return loadForType(ls.schemaJson.get(TYPE));
                        }
                    });
        }

        ls.schemaJson.find(ID).map(JsonElement::asString).ifPresent(builder::id);
        ls.schemaJson.find(TITLE).map(JsonElement::asString).ifPresent(builder::title);
        ls.schemaJson.find(DESCRIPTION).map(JsonElement::asString).ifPresent(builder::description);
        builder.schemaLocation(ls.jsonApi.pointer(ls.pointerToCurrentObj).toURIFragment());
        return builder;
    }

    Schema.Builder loadForType(JsonElement<?> element) {
        if (element.type() == JsonSchemaType.Array) {
            return buildAnyOfSchemaForMultipleTypes();
        } else {
            return loadForExplicitType(element.asString());
        }
    }

    Schema.Builder<?> loadChild(JsonObject<?> childJson) {
        //todo:ericm Loading state?
        SchemaLoaderBuilder childBuilder = ls.initChildLoader()
                .schemaJson(childJson)
                .pointerToCurrentObj(childJson.path());
        if (childJson.has(ID)) {
            URI childURL = ReferenceResolver.resolve(this.ls.id, childJson.get(ID).asString());
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
            return new StringSchemaLoader(ls).load().requiresString(false);
        }
        return null;
    }

    private CombinedSchema.Builder buildAnyOfSchemaForMultipleTypes() {
        JsonArray<?> subtypeJsons = ls.schemaJson.get(TYPE).asArray();
        Collection<Schema> subschemas = new ArrayList<>(subtypeJsons.length());
        subtypeJsons.forEach(element -> {
            subschemas.add(loadForExplicitType(element.asString()).build());
        });
        return CombinedSchema.anyOf(subschemas);
    }

    private EnumSchema.Builder buildEnumSchema() {
        Set<JsonElement<?>> possibleValues = new HashSet<>();
        ls.schemaJson.get(ENUM).asArray()
                .forEach(possibleValues::add);
        return EnumSchema.builder().possibleValues(possibleValues);
    }

    private NotSchema.Builder buildNotSchema() {
        Schema mustNotMatch = loadChild(ls.schemaJson.get(NOT).asObject()).build();
        return NotSchema.builder().mustNotMatch(mustNotMatch);
    }

    private Schema.Builder<?> buildSchemaWithoutExplicitType() {
        if (ls.schemaJson.isEmpty()) {
            return EmptySchema.builder();
        }
        if (ls.schemaJson.has($REF)) {
            String ref = ls.schemaJson.get($REF).asString();
            return new ReferenceLookup(ls).lookup(ref, ls.schemaJson);
        }
        Schema.Builder<?> rval = sniffSchemaByProps();
        if (rval != null) {
            return rval;
        }
        if (ls.schemaJson.has(NOT)) {
            return buildNotSchema();
        }
        return EmptySchema.builder();
    }

    private NumberSchema.Builder buildNumberSchema() {
        NumberSchema.Builder builder = NumberSchema.builder();
        ls.schemaJson.find(MINIMUM).map(JsonElement::asNumber).ifPresent(builder::minimum);
        ls.schemaJson.find(MAXIMUM).map(JsonElement::asNumber).ifPresent(builder::maximum);
        ls.schemaJson.find(MULTIPLE_OF).map(JsonElement::asNumber).ifPresent(builder::multipleOf);
        ls.schemaJson.find(EXCLUSIVE_MINIMUM).map(JsonElement::asBoolean)
                .ifPresent(builder::exclusiveMinimum);
        ls.schemaJson.find(EXCLUSIVE_MAXIMUM).map(JsonElement::asBoolean)
                .ifPresent(builder::exclusiveMaximum);
        return builder;
    }

    private Schema.Builder<?> loadForExplicitType(final String typeString) {
        switch (typeString) {
            case "string":
                return new StringSchemaLoader(ls).load();
            case "integer":
                return buildNumberSchema().requiresInteger(true);
            case "number":
                return buildNumberSchema();
            case "boolean":
                return BooleanSchema.builder();
            case "null":
                return NullSchema.builder();
            case "array":
                return buildArraySchema();
            case "object":
                return buildObjectSchema();
            default:
                throw new SchemaException(String.format("unknown type: [%s]", typeString));
        }
    }

    private ObjectSchema.Builder buildObjectSchema() {
        return new ObjectSchemaLoader(ls, this).load();
    }

    private ArraySchema.Builder buildArraySchema() {
        return new ArraySchemaLoader(ls, this).load();
    }

    private boolean schemaIsOfType(JsonSchemaType type) {
        return Arrays.stream(JsonSchemaProperty.values())
                .filter(p -> p.appliesToType(type))
                .anyMatch(ls.schemaJson::has);
    }

    /**
     * Builder class for {@link SchemaLoader}.
     */
    public static class SchemaLoaderBuilder {

        SchemaClient httpClient = new DefaultSchemaClient();

        JsonApi jsonApi;

        JsonObject schemaJson;

        JsonObject rootSchemaJson;

        Map<String, ReferenceSchema.Builder> pointerSchemas = new HashMap<>();

        URI id;

        List<String> pointerToCurrentObj = emptyList();

        public SchemaLoader build() {
            return new SchemaLoader(this);
        }

        public SchemaLoaderBuilder httpClient(SchemaClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public SchemaLoaderBuilder jsonApi(JsonApi jsonApi) {
            this.jsonApi = jsonApi;
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

        SchemaLoaderBuilder pointerToCurrentObj(List<String> pointerToCurrentObj) {
            this.pointerToCurrentObj = requireNonNull(pointerToCurrentObj);
            return this;
        }

        public JsonObject getSchemaJson() {
            return schemaJson;
        }

        public JsonObject getRootSchemaJson() {
            return rootSchemaJson;
        }
    }
}
