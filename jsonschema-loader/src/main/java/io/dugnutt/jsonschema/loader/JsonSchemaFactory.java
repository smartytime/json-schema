package io.dugnutt.jsonschema.loader;

import com.google.common.base.Strings;
import io.dugnutt.jsonschema.loader.reference.DefaultSchemaClient;
import io.dugnutt.jsonschema.loader.reference.SchemaCache;
import io.dugnutt.jsonschema.loader.reference.SchemaClient;
import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.SchemaFactory;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.six.SchemaUtils;
import io.dugnutt.jsonschema.six.URIUtils;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.spi.JsonProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.loader.SchemaLoadingContext.COMBINED_SCHEMA_KEYWORDS;
import static io.dugnutt.jsonschema.loader.SchemaLoadingContext.createModelFor;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DEFAULT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DESCRIPTION;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TITLE;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TYPE;
import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.SchemaLocation.DUGNUTT_UUID_SCHEME;

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

    @Wither
    private final Charset charset;

    @Wither
    private SchemaCache schemaCache;

    public Schema createSchema(SchemaLoadingContext schemaModel) {
        // First, get from cache, or
        return schemaCache.getSchema(schemaModel.getLocation())
                .orElseGet(() -> {
                    // If not in cache, then create.  Load in the baseDocument so it will be passed
                    Schema schema = createSchemaBuilder(schemaModel).build();
                    schemaCache.cacheSchema(schemaModel.getLocation(), schema);
                    return schema;
                });
    }

    public JsonSchemaBuilder createSchemaBuilder(SchemaLoadingContext schemaContext) {
        return createSchemaBuilder(schemaContext.schemaJson())
                .currentDocument(schemaContext.rootSchemaJson())
                .schemaFactory(this)
                .location(schemaContext.getLocation());
    }

    public JsonSchemaBuilder createSchemaBuilder(JsonValue value, JsonPath path) {
        checkNotNull(value, "value must not be null");
        checkNotNull(path, "path must not be null");
        final PathAwareJsonValue pathAwareJsonValue = new PathAwareJsonValue(value, path);
        return createSchemaBuilder(pathAwareJsonValue);
    }

    public JsonSchemaBuilder createSchemaBuilder(PathAwareJsonValue schemaJson) {

        // ##########################
        // $ref: Overrides everything
        // ##########################

        if (schemaJson.has($REF)) {
            //Ignore all other keywords when encountering a ref
            String ref = schemaJson.getString($REF);
            return jsonSchemaBuilder().ref(ref, this);
        }

        final JsonSchemaBuilder schemaBuilder = schemaJson.findString($ID)
                .map(Schema::jsonSchemaBuilderWithId)
                .orElse(Schema.jsonSchemaBuilder());

        // ############################
        // type: either string or array
        // ############################

        schemaJson.findByKey(TYPE).ifPresent(typeJsonValue -> {
            final JsonPath typePath = schemaJson.getPath().child(TYPE.key());
            switch (typeJsonValue.getValueType()) {
                case STRING:
                    final JsonSchemaType typeEnum;
                    try {
                        typeEnum = JsonSchemaType.fromString(schemaJson.getString(TYPE));
                    } catch (SchemaException e) {
                        throw new SchemaException(typePath.toURIFragment(), e.getMessage());
                    }
                    schemaBuilder.type(typeEnum);
                    break;
                case ARRAY:
                    schemaJson.expectArray(TYPE).getValuesAs(JsonString.class).stream()
                            .map(JsonString::getString)
                            .map(JsonSchemaType::fromString)
                            .forEach(schemaBuilder::type);
                    break;
                default:
                    throw new UnexpectedValueException(typePath, typeJsonValue, ValueType.STRING, ValueType.ARRAY);
            }
        });

        // ############################
        // title, description
        // ############################

        schemaJson.findByKey(DEFAULT).ifPresent(schemaBuilder::defaultValue);
        schemaJson.findString(TITLE).ifPresent(schemaBuilder::title);
        schemaJson.findString(DESCRIPTION).ifPresent(schemaBuilder::description);

        // ##########################
        // const, enum, not
        // ##########################

        schemaJson.findByKey(CONST).ifPresent(schemaBuilder::constValue);
        schemaJson.findArray(ENUM).ifPresent(schemaBuilder::enumValues);
        schemaJson.findPathAware(NOT).map(this::createSchemaBuilder).ifPresent(schemaBuilder::notSchema);

        // #########################
        // allOf, anyOf, oneOf
        // #########################

        COMBINED_SCHEMA_KEYWORDS.forEach(keyword -> schemaJson.streamPathAwareArrayItems(keyword)
                .map(this::createSchemaBuilder)
                .forEach(combinedSchema -> schemaBuilder.combinedSchema(keyword, combinedSchema)));

        // ##########################################
        // type-specific keywords
        //
        // Only loads when there are keywords present
        // ##########################################

        StringKeywordsFactoryHelper.appendStringKeywords(schemaJson, schemaBuilder);
        NumberKeywordsFactoryHelper.appendNumberKeywords(schemaJson, schemaBuilder);
        ObjectKeywordsFactoryHelper.appendObjectKeywords(schemaJson, schemaBuilder, this);
        ArrayKeywordsFactoryHelper.appendArrayKeywords(schemaJson, schemaBuilder, this);

        return schemaBuilder;
    }

    @Override
    public Schema loadRefSchema(Schema referencedFrom, URI refURI, @Nullable JsonObject currentDocument) {
        // Cache ahead to deal with any infinite recursion.
        final SchemaLocation currentLocation = referencedFrom.getLocation();
        schemaCache.cacheSchema(currentLocation.getAbsoluteURI(), referencedFrom);
        final URI absoluteReferenceURI = currentLocation.getResolutionScope().resolve(refURI);
        final Optional<Schema> cachedSchema = schemaCache.getSchema(absoluteReferenceURI);
        if (cachedSchema.isPresent()) {
            return cachedSchema.get();
        }

        final URI documentURI = currentLocation.getDocumentURI();
        final URI remoteDocumentURI = absoluteReferenceURI.resolve("#");

        final JsonSchemaBuilder schemaBuilder = findRefInDocument(documentURI, absoluteReferenceURI, currentDocument)
                .orElseGet(() -> findRefInRemoteDocument(remoteDocumentURI, absoluteReferenceURI));
        final Schema refSchema = schemaBuilder.build();
        schemaCache.cacheSchema(absoluteReferenceURI, refSchema);
        return refSchema;
    }

    @Override
    public Optional<Schema> findCachedSchema(URI schemaURI) {
        return schemaCache.getSchema(schemaURI);
    }

    public Schema load(JsonObject schemaJson) {
        checkNotNull(schemaJson, "schemaJson must not be null");
        SchemaLoadingContext modelToLoad = createModelFor(schemaJson);
        schemaCache.cacheDocument(modelToLoad.getLocation().getAbsoluteURI(), schemaJson);
        return createSchema(modelToLoad);
    }

    public Schema load(InputStream inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        final JsonObject baseDocument = provider.createReader(inputJson).readObject();
        return load(baseDocument);
    }

    public Schema load(String inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        return load(provider.createReader(new StringReader(inputJson)).readObject());
    }

    public JsonSchemaFactory withPreloadedSchema(InputStream preloadedSchema) {
        checkNotNull(preloadedSchema, "preloadedSchema must not be null");
        final JsonObject jsonObject = provider.createReader(preloadedSchema).readObject();
        load(jsonObject);
        return this;
    }

    JsonObject loadDocument(URI absoluteReferenceURI) {
        final URI remoteDocumentURI = URIUtils.withoutFragment(absoluteReferenceURI);
        final JsonObject targetDocument;

        targetDocument = schemaCache.lookupDocument(remoteDocumentURI)
                .orElseGet(() -> {
                    String scheme = absoluteReferenceURI.getScheme().toLowerCase();

                    if (!scheme.toLowerCase().startsWith("http")) {
                        if (DUGNUTT_UUID_SCHEME.equals(scheme)) {
                            String fragment = "#" + firstNonNull(absoluteReferenceURI.getFragment(), "");
                            throw new SchemaException(URI.create("#"), "Unable to find ref '%s' within document", fragment);
                        }
                        throw new SchemaException(absoluteReferenceURI, "Couldn't resolve ref within document, but can't load non-http scheme");
                    }

                    //If that doesn't work, look up schema using client
                    try (InputStream inputStream = httpClient.fetchSchema(remoteDocumentURI)) {
                        final JsonObject jsonObject = provider.createReader(inputStream).readObject();
                        schemaCache.cacheDocument(remoteDocumentURI, jsonObject);
                        return jsonObject;
                    } catch (IOException e) {
                        throw new SchemaException(absoluteReferenceURI, "Error while fetching document '" + absoluteReferenceURI + "'");
                    }
                });

        if (targetDocument == null) {
            throw new SchemaException(absoluteReferenceURI, "Unable to get document: " + absoluteReferenceURI);
        }

        return targetDocument;
    }

    JsonSchemaBuilder findRefInRemoteDocument(URI remoteDocumentURI, URI referenceURI) {
        final JsonObject remoteDocument = loadDocument(remoteDocumentURI);
        return findRefInDocument(remoteDocumentURI, referenceURI, remoteDocument)
                .orElseThrow(() -> new SchemaException(referenceURI, "Error loading fragment '#%s' from document '%s'", referenceURI.getFragment(), remoteDocument));
    }

    Optional<JsonSchemaBuilder> findRefInDocument(URI documentURI, URI referenceURI, JsonObject parentDocument) {
        checkArgument(referenceURI.isAbsolute(), "Reference URI must be absolute");
        checkArgument(documentURI.isAbsolute(), "Document URI must be absolute");
        if (parentDocument == null) {
            parentDocument = loadDocument(referenceURI);
        }

        //Remove any fragments from the parentDocument URI
        documentURI = URIUtils.withoutFragment(documentURI);

        // Relativizing strips the path down to only the difference between the documentURI and referenceURI.
        // This will tell us whether the referenceURI is naturally scoped within the parentDocument.
        String relativeURL = documentURI.relativize(referenceURI).toString();

        final JsonPath pathWithinDocument;
        if (Strings.isNullOrEmpty(relativeURL) || "#".equals(relativeURL)) {
            // The parentDocument is the target
            pathWithinDocument = JsonPath.rootPath();
        } else if (SchemaUtils.isJsonPointer(relativeURL)) {
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

            SchemaLocation fetchedDocumentLocation = SchemaLocation.locationBuilder()
                    .id(JsonUtils.extract$IdFromObject(schemaObject))
                    .documentURI(documentURI)
                    .resolutionScope(documentURI)
                    .jsonPath(pathWithinDocument)
                    .build();

            SchemaLoadingContext loadingContext = SchemaLoadingContext.schemaContextBuilder()
                    .location(fetchedDocumentLocation)
                    .rootSchemaJson(parentDocument)
                    .schemaJson(schemaObject)
                    .build();

            return Optional.of(this.createSchemaBuilder(loadingContext));
        }
        return Optional.empty();
    }

    public static JsonSchemaFactory schemaFactory() {
        return schemaFactory(JsonProvider.provider());
    }

    public static JsonSchemaFactory schemaFactory(JsonProvider jsonProvider) {
        return new JsonSchemaFactory(jsonProvider, new DefaultSchemaClient(), UTF8, SchemaCache.schemaCacheBuilder().build());
    }
}
