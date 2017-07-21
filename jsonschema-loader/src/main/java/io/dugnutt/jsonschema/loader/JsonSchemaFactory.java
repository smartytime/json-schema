package io.dugnutt.jsonschema.loader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.dugnutt.jsonschema.loader.reference.DefaultSchemaClient;
import io.dugnutt.jsonschema.loader.reference.SchemaCache;
import io.dugnutt.jsonschema.loader.reference.SchemaClient;
import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.NullSchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.SchemaFactory;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;

/**
 * Schema factories are responsible for extracting values from a json-object to produce an immutable {@link Schema}
 * instance.  This also includes looking up any referenced schemas.
 */
@Getter
@AllArgsConstructor
public class JsonSchemaFactory implements SchemaFactory {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    @Wither
    private final JsonProvider provider;

    @Wither
    private final SchemaClient httpClient;

    private final LinkedList<URI> creationStack = new LinkedList<>();

    @Wither
    private final Charset charset;

    @Wither
    private SchemaCache schemaCache;

    public static JsonSchemaFactory schemaFactory() {
        return schemaFactory(JsonProvider.provider());
    }

    public static JsonSchemaFactory schemaFactory(JsonProvider jsonProvider) {
        return new JsonSchemaFactory(jsonProvider, new DefaultSchemaClient(), UTF8, SchemaCache.builder().build());
    }

    public Schema.Builder createBuilderForExplicitSchemaType(final SchemaLoadingContext schemaModel, final JsonSchemaType schemaType) {
        return createBuilderForSchemaType(schemaModel, schemaType, true);
    }

    public Schema.Builder createBuilderForSchemaType(final SchemaLoadingContext schemaModel, final JsonSchemaType schemaType,
                                                     boolean explicitlyDeclaredType) {
        switch (schemaType) {
            case STRING:
                return StringSchemaFactory.createStringSchemaBuilder(schemaModel).requiresString(explicitlyDeclaredType);
            case NUMBER:
                return NumberSchemaFactory.createNumberSchemaBuilder(schemaModel).requiresNumber(explicitlyDeclaredType);
            case INTEGER:
                return NumberSchemaFactory.createNumberSchemaBuilder(schemaModel).requiresInteger(explicitlyDeclaredType);
            case BOOLEAN:
                return BooleanSchema.builder().location(schemaModel.getLocation());
            case NULL:
                return NullSchema.builder().location(schemaModel.getLocation());
            case ARRAY:
                return ArraySchemaFactory.createArraySchemaBuilder(schemaModel, this).requiresArray(explicitlyDeclaredType);
            case OBJECT:
                return ObjectSchemaFactory.createObjectSchemaBuilder(schemaModel, this).requiresObject(explicitlyDeclaredType);
            default:
                throw schemaModel.createSchemaException(String.format("unknown type: [%s]", schemaType));
        }

    }

    public Schema createSchema(SchemaLoadingContext schemaModel) {
        // First, get from cache, or
        return schemaCache.getSchema(schemaModel.getLocation())
                .orElseGet(() -> {
                    // If not in cache, then create.
                    Schema schema = createSchemaBuilder(schemaModel).build();
                    schemaCache.cacheSchema(schemaModel.getLocation(), schema);
                    return schema;
                });
    }

    @Override
    public Schema dereferenceSchema(URI currentDocumentURI, ReferenceSchema referenceSchema, JsonObject currentDocument) {
        checkArgument(referenceSchema.getAbsoluteReferenceURI().isAbsolute(), "reference must not absolute");

        //todo:ericm - Do we need to cache so aggressively?
        URI absoluteReferenceURI = referenceSchema.getAbsoluteReferenceURI();
        schemaCache.cacheSchema(referenceSchema.getLocation().getAbsoluteURI(), referenceSchema);
        // Look for a cached schema
        return schemaCache.getSchema(absoluteReferenceURI)
                .orElseGet(() -> {
                    // First, look locally within the current document
                    return resolveReferenceWithinDocument(currentDocumentURI, absoluteReferenceURI, currentDocument)
                            .orElseGet(() -> {
                                //If that doesn't work, look for the raw document in the cache
                                final URI remoteDocumentURI = absoluteReferenceURI.resolve("#");
                                JsonObject remoteDocument = schemaCache.lookupDocument(remoteDocumentURI)
                                        .orElseGet(() -> {
                                            // Lastly, fetch over the network
                                            String schema = absoluteReferenceURI.getScheme().toLowerCase();
                                            if (SchemaLocation.DUGNUTT_UUID_SCHEME.equals(schema)) {
                                                String commonPrefix = Strings.commonPrefix(absoluteReferenceURI.toString(), currentDocumentURI.toString());
                                                final String relativizedURL;
                                                if (commonPrefix.length() > 0) {
                                                    relativizedURL = absoluteReferenceURI.toString().substring(commonPrefix.length() - 1);
                                                } else {
                                                    relativizedURL = absoluteReferenceURI.toString();
                                                }
                                                throw new SchemaException(URI.create("#"), "Unable to find ref '%s' within document", relativizedURL);
                                            } else if (!schema.toLowerCase().startsWith("http")) {
                                                throw new SchemaException(absoluteReferenceURI, "Couldn't resolve ref within document, but can't load non-http schema");
                                            }

                                            //If that doesn't work, look up schema using client

                                            try (InputStream inputStream = httpClient.get(absoluteReferenceURI.toString())) {
                                                final JsonObject jsonObject = provider.createReader(inputStream).readObject();
                                                schemaCache.cacheDocument(remoteDocumentURI, jsonObject);
                                                return jsonObject;
                                            } catch (IOException e) {
                                                throw new SchemaException(absoluteReferenceURI, "Error while fetching document '" + absoluteReferenceURI + "'");
                                            }
                                        });

                                return resolveReferenceWithinDocument(remoteDocumentURI, absoluteReferenceURI, remoteDocument)
                                        .orElseThrow(() -> new SchemaException(absoluteReferenceURI, "Error loading fragment '%s' from document '%s'",
                                                absoluteReferenceURI.getFragment(), absoluteReferenceURI));
                            });
                });
    }

    public Schema load(InputStream inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        return load(provider.createReader(inputJson).readObject());
    }

    public Schema load(String inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        return load(provider.createReader(new StringReader(inputJson)).readObject());
    }

    public Schema load(JsonObject schemaJson) {
        checkNotNull(schemaJson, "schemaJson must not be null");
        SchemaLoadingContext modelToLoad = SchemaLoadingContext.createModelFor(schemaJson);
        schemaCache.cacheDocument(modelToLoad.getLocation().getAbsoluteURI(), schemaJson);
        return createSchema(modelToLoad);
    }

    public JsonSchemaFactory withPreloadedSchema(InputStream preloadedSchema) {
        checkNotNull(preloadedSchema, "preloadedSchema must not be null");
        final JsonObject jsonObject = provider.createReader(preloadedSchema).readObject();
        load(jsonObject);
        return this;
    }

    @VisibleForTesting
    Optional<Schema> resolveReferenceWithinDocument(URI documentURI, URI referenceURI, JsonObject parentDocument) {
        checkArgument(referenceURI.isAbsolute(), "Reference URI must be absolute");
        checkArgument(documentURI.isAbsolute(), "Document URI must be absolute");

        //Remove any fragments from the parentDocument URI
        documentURI = documentURI.resolve("#");

        // Relativizing strips the path down to only the difference between the documentURI and referenceURI.
        // This will tell us whether the referenceURI is naturally scoped within the parentDocument.
        String relativeURL = documentURI.relativize(referenceURI).toString();

        final JsonPath pathWithinDocument;
        if (Strings.isNullOrEmpty(relativeURL) || "#".equals(relativeURL)) {
            // The parentDocument is the target
            pathWithinDocument = JsonPath.rootPath();
        } else if (relativeURL.startsWith("#/")) {
            //This is a json fragment
            pathWithinDocument = JsonPath.parseFromURIFragment(relativeURL);
        } else {
            //This must be a reference $id somewhere in the parentDocument.
            pathWithinDocument = schemaCache.resolveURIToDocumentUsingLocalIdentifiers(documentURI, referenceURI, parentDocument)
                    .orElse(null);
        }
        if (pathWithinDocument != null) {
            final JsonPointer pointer = provider.createPointer(pathWithinDocument.toJsonPointer());
            final JsonObject schemaObject;
            if (!pointer.containsValue(parentDocument)) {
                throw new SchemaException(referenceURI, "Unable to resolve '#" + relativeURL + "' as JSON Pointer within '" + documentURI + "'");
            } else {
                schemaObject = pointer.getValue(parentDocument).asJsonObject();
            }

            SchemaLocation fetchedDocumentLocation = SchemaLocation.builder()
                    .id(JsonUtils.extract$IdFromObject(schemaObject))
                    .documentURI(documentURI)
                    .resolutionScope(documentURI)
                    .jsonPath(pathWithinDocument)
                    .build();

            SchemaLoadingContext loadingContext = SchemaLoadingContext.builder()
                    .location(fetchedDocumentLocation)
                    .rootSchemaJson(parentDocument)
                    .schemaJson(schemaObject)
                    .build();

            return Optional.of(this.createSchema(loadingContext));
        }
        return Optional.empty();
    }

    private Schema.Builder<?> createSchemaBuilder(SchemaLoadingContext schemaModel) {
        PathAwareJsonValue schemaJson = schemaModel.schemaJson;

        final Schema.Builder<?> schemaBuilder = determineAndCreateSchemaBuilder(schemaModel);

        schemaJson.findString(JsonSchemaKeyword.TITLE).ifPresent(schemaBuilder::title);
        schemaJson.findString(JsonSchemaKeyword.DESCRIPTION).ifPresent(schemaBuilder::description);

        schemaJson.findArray(ENUM).ifPresent(schemaBuilder::enumValues);
        schemaJson.findByKey(CONST).ifPresent(schemaBuilder::constValue);
        schemaModel.childModel(NOT).map(this::createSchema).ifPresent(schemaBuilder::notSchema);

        Stream<Schema> allOfSchemas = schemaModel.streamChildSchemaModels(ALL_OF, JsonValue.ValueType.ARRAY)
                .map(this::createSchema);
        schemaBuilder.allOfSchemas(allOfSchemas);

        Stream<Schema> anyOfSchemas = schemaModel.streamChildSchemaModels(ANY_OF, JsonValue.ValueType.ARRAY)
                .map(this::createSchema);
        schemaBuilder.anyOfSchemas(anyOfSchemas);

        Stream<Schema> oneOfSchemas = schemaModel.streamChildSchemaModels(ONE_OF, JsonValue.ValueType.ARRAY)
                .map(this::createSchema);
        schemaBuilder.oneOfSchemas(oneOfSchemas);

        return schemaBuilder;
    }

    private Schema.Builder<?> determineAndCreateSchemaBuilder(SchemaLoadingContext schemaModel) {
        checkNotNull(schemaModel, "model must not be null");

        final PathAwareJsonValue schemaJson = schemaModel.schemaJson;

        if (schemaModel.isRefSchema()) {
            //Ignore all other keywords when encountering a ref
            String ref = schemaJson.getString($REF);
            return ReferenceSchema.builder()
                    .referenceSchemaLoader(this, schemaModel.getRootSchemaJson())
                    .referencedURL(ref)
                    .location(schemaModel.getLocation())
                    ;
        } else if (schemaModel.hasExplicitTypeValue()) {
            // If this is for an explicit type, we can effectively ignore all other keywords, and only
            // load the keywords for this type.
            return createBuilderForExplicitSchemaType(schemaModel, schemaModel.getExplicitType());
        } else if (schemaModel.hasExplicitTypeArray()) {
            // For each explicitly declared type in the array, extract all properties for that type out of the
            // json.  Create a list of these, and during validation we'll ensure that at least one is valid.
            return MultipleTypeSchemaFactory.createExplicitTypeBuilder(schemaModel, this);

            // URI refURI = ReferenceScopeResolver.resolveScope(schemaModel.getResolutionScope(), ref);
            //
            // schemaCache.getSchema(refURI, schemaModel.getResolutionScope())
            //     .orElse();
            //

            // return new SchemaCache(schemaModel).createReferenceSchemaBuilder(ref, schemaJson);
        } else {
            //Fallback is to scan properties and load any schema types that are present (have at least one keyword)
            return MultipleTypeSchemaFactory.createSchemaBuilderFromProperties(schemaModel, this);
        }
        // if (schemaModel.isEnumSchema()) {
        //     final JsonArray possibleEnumValues = schemaModel.getSchemaJson().expectArray(ENUM);
        //     return EnumSchema.builder().possibleValues(possibleEnumValues);
        // } else if (schemaModel.isCombinedSchema()) {
        //     return new CombinedSchemaFactory(schemaModel, this)
        //             .combinedSchemaBuilder();
        // } else if (schemaModel.isEmpty()) {
        //     return new EmptySchema.Builder();
        // } else if (schemaModel.isRefSchema()) {
        //
        // } else if (schemaModel.hasExplicitTypeArray()) {
        //     JsonArray subtypeJsons = schemaJson.expectArray(TYPE);
        //     List<Schema> subSchemas = subtypeJsons.getValuesAs(JsonString.class).stream()
        //             .map(jsonString -> {
        //                 JsonSchemaType type = JsonSchemaType.fromString(jsonString.getString());
        //                 return createBuilderForExplicitSchemaType(schemaModel, type).build();
        //             })
        //             .collect(Collectors.toList());
        //     return CombinedSchema.anyOf(subSchemas);
        // } else if (schemaModel.hasExplicitTypeValue()) {
        //     JsonSchemaType explicitType = schemaModel.getExplicitType();
        //     return createBuilderForExplicitSchemaType(schemaModel, explicitType);
        // } else if (schemaModel.isSchemaOf(JsonSchemaType.ARRAY)) {
        //     return buildArraySchema(schemaModel).requiresArray(false);
        // } else if (schemaModel.isSchemaOf(JsonSchemaType.OBJECT)) {
        //     return buildObjectSchema(schemaModel).requiresObject(false);
        // } else if (schemaModel.isSchemaOf(JsonSchemaType.NUMBER)) {
        //     return buildNumberSchema(schemaModel).requiresNumber(false);
        // } else if (schemaModel.isSchemaOf(JsonSchemaType.STRING)) {
        //     return new StringSchemaFactory(schemaModel).load().requiresString(false);
        // } else if (schemaModel.isNotSchema()) {
        //     Schema mustNotMatch = createChildSchemaBuilder(schemaModel, NOT).build();
        //     return NotSchema.builder().mustNotMatch(mustNotMatch);
        // } else {
        //     throw schemaModel.createSchemaException("Unable to determine type");
        // }
    }
}
