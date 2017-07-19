package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.loader.reference.DefaultSchemaClient;
import io.dugnutt.jsonschema.loader.reference.SchemaCache;
import io.dugnutt.jsonschema.loader.reference.SchemaClient;
import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.JsonPointerPath;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.NullSchema;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;
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
public class JsonSchemaFactory {

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

    public Schema.Builder createBuilderForExplicitSchemaType(final SchemaLoaderModel schemaModel, final JsonSchemaType schemaType) {
        return createBuilderForSchemaType(schemaModel, schemaType, true);
    }

    public Schema.Builder createBuilderForSchemaType(final SchemaLoaderModel schemaModel, final JsonSchemaType schemaType,
                                                     boolean explicitlyDeclaredType) {
        switch (schemaType) {
            case STRING:
                return StringSchemaFactory.createStringSchemaBuilder(schemaModel).requiresString(explicitlyDeclaredType);
            case NUMBER:
                return NumberSchemaFactory.createNumberSchemaBuilder(schemaModel).requiresNumber(explicitlyDeclaredType);
            case INTEGER:
                return NumberSchemaFactory.createNumberSchemaBuilder(schemaModel).requiresInteger(explicitlyDeclaredType);
            case BOOLEAN:
                return BooleanSchema.builder(schemaModel.getLocation());
            case NULL:
                return NullSchema.builder(schemaModel.getLocation());
            case ARRAY:
                return ArraySchemaFactory.createArraySchemaBuilder(schemaModel, this).requiresArray(explicitlyDeclaredType);
            case OBJECT:
                return ObjectSchemaFactory.createObjectSchemaBuilder(schemaModel, this).requiresObject(explicitlyDeclaredType);
            default:
                throw schemaModel.createSchemaException(String.format("unknown type: [%s]", schemaType));
        }
    }

    public Schema createSchema(SchemaLoaderModel schemaModel) {

        // this.creationStack.push(schemaModel.getLocation());
        Optional<Schema> cachedSchema = schemaCache.getSchema(schemaModel.getLocation());
        final Schema schema;
        if (cachedSchema.isPresent()) {
            schema = cachedSchema.get();
        } else {
            schema = createSchemaBuilder(schemaModel).build();
            schemaCache.cacheSchema(schemaModel.getLocation(), schema);
        }
        // this.creationStack.pop();

        return schema;
    }

    // Schema.Builder builder;
    // if (loadingState.schemaJson.has(JsonSchemaProperty.ENUM)) {
    //     builder = buildEnumSchema();
    // } else {
    //     builder = new CombinedSchemaLoader(loadingState, this).load()
    //             .orElseGet(() -> {
    //                 if (!loadingState.schemaJson.has(JsonSchemaProperty.TYPE) || loadingState.schemaJson.has(JsonSchemaProperty.$REF)) {
    //                     return buildSchemaWithoutExplicitType();
    //                 } else {
    //                     return loadForType(loadingState.schemaJson.get(JsonSchemaProperty.TYPE.key()));
    //                 }
    //             });
    // }
    //
    // return builder;

    //     } else if (!schemaJson.has(TYPE) || schemaJson.has($REF)) {
    //         return buildSchemaWithoutExplicitType();
    //     } else {
    //         return loadForType(schemaModel.schemaJson.getString(TYPE.key()));
    //     }
    //     this.createEnumSchemaBuilder(schemaModel)
    //             .orElseGet()
    //
    //
    //     Schema.Builder<?> builder =
    //             .orElseGet(() -> {
    //                 return
    //                         .orElseGet(() -> {
    //
    //                         });
    //             });
    //
    //     if (schemaModel.schemaJson.has(ENUM)) {
    //         builder = enumSchemaBuilder();
    //     } else {
    //
    //     }
    //
    //     schemaModel.schemaJson.findString(ID).map(JsonString::getString).ifPresent(builder::id);
    //     schemaModel.schemaJson.findString(TITLE).map(JsonString::getString).ifPresent(builder::title);
    //     schemaModel.schemaJson.findString(DESCRIPTION).map(JsonString::getString).ifPresent(builder::description);
    //     builder.schemaLocation(schemaModel.currentJsonPath.toURIFragment());
    //     return builder;
    // }

    // Schema.Builder loadForType(JsonValue element) {
    // if (element.getValueType() == ARRAY) {
    //     return buildAnyOfSchemaForMultipleTypes();
    // } else if (element.getValueType() == STRING) {
    //     final String stringType = ((JsonString) element).getString();
    //
    // } else {
    //     throw new UnexpectedValueException(element, ARRAY, STRING);
    // }
    // }

    // public Schema.Builder<?> createChildSchemaBuilder(SchemaLoaderModel parentModel, JsonSchemaProperty childKey) {
    //     return this.loadSchema(parentModel.childModel(childKey));
    // }

    public Schema load(JsonObject schemaJson) {
        checkNotNull(schemaJson, "schemaJson must not be null");
        SchemaLoaderModel modelToLoad = SchemaLoaderModel.createModelFor(schemaJson);
        return createSchema(modelToLoad);
    }

    // public EnumSchema.Builder enumSchemaBuilder(JsonArray enumArray) {
    //     checkNotNull(enumArray, "enumArray must not be null");
    //     return ;
    // }

    public Schema load(InputStream inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        return load(provider.createReader(inputJson).readObject());
    }

    public Schema load(String inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        return load(provider.createReader(new StringReader(inputJson)).readObject());
    }

    /**
     * Returns the absolute URI without its fragment part.
     *
     * @param fullUri the abslute URI
     * @return the URI without the fragment part
     */
    static URI withoutFragment(final String fullUri) {
        int hashmarkIdx = fullUri.indexOf('#');
        String rval;
        if (hashmarkIdx == -1) {
            rval = fullUri;
        } else {
            rval = fullUri.substring(0, hashmarkIdx);
        }
        try {
            return new URI(rval);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Schema.Builder<?> createSchemaBuilder(SchemaLoaderModel schemaModel) {
        FluentJsonObject schemaJson = schemaModel.schemaJson;

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

    private Schema.Builder<?> determineAndCreateSchemaBuilder(SchemaLoaderModel schemaModel) {
        checkNotNull(schemaModel, "model must not be null");

        final FluentJsonObject schemaJson = schemaModel.schemaJson;

        if (schemaModel.isRefSchema()) {
            //Ignore all other keywords when encountering a ref
            String ref = schemaJson.getString($REF);
            return ReferenceSchema.builder(schemaModel.getLocation())
                    .referenceSchemaLoader(referenceSchema -> {

                        schemaCache.cacheSchema(referenceSchema.getLocation(), referenceSchema);

                        URI referenceURI = referenceSchema.getAbsoluteReferenceURI();
                        return schemaCache.getSchema(referenceURI)
                                .orElseGet(() -> {

                            if (!referenceURI.toString().startsWith("#")) {
                                JsonPointerResolver.QueryResult queryResult = JsonPointerResolver.forURL(httpClient, referenceURI.toString(), provider)
                                        .query();

                                JsonObject fetchedJson = queryResult.getQueryResult();
                                JsonObject containingDocument = queryResult.getContainingDocument();

                                //Root document
                                String rootId;
                                if (containingDocument.containsKey($ID.key())) {
                                    rootId = containingDocument.getString($ID.key());
                                } else if (containingDocument.containsKey("id")) {
                                    rootId = containingDocument.getString("id");
                                } else {
                                    rootId = "#";
                                }

                                SchemaLocation parentLocation = SchemaLocation.rootSchemaLocation(rootId);
                                SchemaLocation childPath = parentLocation.withChildPath(referenceURI);

                                SchemaLoaderModel loaderModel = SchemaLoaderModel.builder()
                                        .location(childPath)
                                        .rootSchemaJson(new FluentJsonObject(containingDocument, new JsonPointerPath(parentLocation.getJsonPath())))
                                        .schemaJson(new FluentJsonObject(fetchedJson, new JsonPointerPath(childPath.getJsonPath())))
                                        .build();

                                return createSchema(loaderModel);
                            } else {
                                String relativeURL = schemaModel.getLocation().getDocumentUri().relativize(referenceURI).toString();
                                JsonPointerResolver.QueryResult query = JsonPointerResolver.forDocument(schemaModel.rootSchemaJson, relativeURL, provider).query();
                                SchemaLocation newLocation = schemaModel.getLocation().toBuilder()
                                        .priorResolutionScope(schemaModel.getLocation().getDocumentUri())
                                        .jsonPath(new JsonPointerPath(relativeURL).jsonPath())
                                        .id(referenceURI)
                                        .build();

                                SchemaLoaderModel loaderModel = schemaModel.toBuilder()
                                        .location(newLocation)
                                        .schemaJson(new FluentJsonObject(query.getQueryResult(), new JsonPointerPath(newLocation.getJsonPath())))
                                        .build();
                                return createSchema(loaderModel);
                            }


                        });
                    })
                    .referencedURL(ref);
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
