package io.dugnutt.jsonschema.loader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.dugnutt.jsonschema.loader.reference.DefaultSchemaClient;
import io.dugnutt.jsonschema.loader.reference.SchemaCache;
import io.dugnutt.jsonschema.loader.reference.SchemaClient;
import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchemaInfo;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaBuildingContext;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.SchemaFactory;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.six.URIUtils;
import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.experimental.var;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonString;
import javax.json.JsonValue.ValueType;
import javax.json.spi.JsonProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.loader.SchemaLoadingContext.COMBINED_SCHEMA_KEYWORDS;
import static io.dugnutt.jsonschema.loader.SchemaLoadingContext.createModelFor;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DESCRIPTION;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TITLE;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TYPE;
import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilder;

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
                    // If not in cache, then create.  Load in the baseDocument so it will propogate everywhere
                    Schema schema = createSchemaBuilder(schemaModel).build(new SchemaBuildingContext());
                    schemaCache.cacheSchema(schemaModel.getLocation(), schema);
                    return schema;
                });
    }

    public JsonSchemaBuilder createSchemaBuilder(SchemaLoadingContext schemaContext) {
        return createSchemaBuilder(schemaContext.schemaJson())
                .currentDocument(schemaContext.rootSchemaJson())
                .schemaInfo(JsonSchemaInfo.builder()
                        .location(schemaContext.getLocation())
                        .containedBy(schemaContext.getLocation())
                        .build());
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

        if (schemaJson.has(TYPE, ValueType.STRING)) {
            final JsonSchemaType typeEnum = JsonSchemaType.fromString(schemaJson.getString(TYPE));
            schemaBuilder.type(typeEnum);
        } else if (schemaJson.has(TYPE, ValueType.ARRAY)) {
            schemaJson.expectArray(TYPE).getValuesAs(JsonString.class).stream()
                    .map(JsonString::getString)
                    .map(JsonSchemaType::fromString)
                    .forEach(schemaBuilder::type);
        }

        // ############################
        // title, description
        // ############################

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
    public Schema dereferenceSchema(SchemaBuildingContext context, URI documentURI, URI absoluteReferenceURI, @Nullable JsonObject currentDocument) {
        final URI remoteDocumentURI = absoluteReferenceURI.resolve("#");

        checkArgument(absoluteReferenceURI.isAbsolute(), "reference must be absolute");

        var targetDocuments = ImmutableMap.<URI, Optional<JsonObject>>builder()
                .put(documentURI, Optional.ofNullable(currentDocument));
        if (!documentURI.equals(remoteDocumentURI)) {
            targetDocuments.put(remoteDocumentURI, Optional.empty());
        }

        // Look for a cached schema
        return schemaCache.getSchema(absoluteReferenceURI)
                .orElseGet(() -> {
                    return targetDocuments.build().entrySet().stream()
                            .map(entry -> {
                                final URI targetDocumentURI = entry.getKey();
                                final JsonObject attemptDocument = loadDocumentIfNecessary(targetDocumentURI, absoluteReferenceURI, entry.getValue().orElse(null));
                                return attemptResolveReferenceWithinDocument(targetDocumentURI, absoluteReferenceURI, attemptDocument)
                                        .map(builder->builder.build(context))
                                        .orElse(null);
                            })
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElseThrow(() -> new SchemaException(absoluteReferenceURI, "Error loading fragment '%s' from document '%s'", absoluteReferenceURI.getFragment(), documentURI));
                });
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

    JsonObject loadDocumentIfNecessary(URI currentDocumentURI, URI absoluteReferenceURI, @Nullable JsonObject providedDocument) {
        //If that doesn't work, look for the raw document in the cache
        final URI remoteDocumentURI = absoluteReferenceURI.resolve("#");
        final JsonObject targetDocument;
        if (providedDocument != null) {
            targetDocument = providedDocument;
        } else {
            targetDocument = schemaCache.lookupDocument(remoteDocumentURI)
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

                        try (InputStream inputStream = httpClient.fetchSchema(absoluteReferenceURI)) {
                            final JsonObject jsonObject = provider.createReader(inputStream).readObject();
                            schemaCache.cacheDocument(remoteDocumentURI, jsonObject);
                            return jsonObject;
                        } catch (IOException e) {
                            throw new SchemaException(absoluteReferenceURI, "Error while fetching document '" + absoluteReferenceURI + "'");
                        }
                    });
        }

        if (targetDocument == null) {
            throw new SchemaException(absoluteReferenceURI, "Unable to get document: " + absoluteReferenceURI);
        }

        return targetDocument;
    }

    @VisibleForTesting
    Optional<JsonSchemaBuilder> attemptResolveReferenceWithinDocument(URI documentURI, URI referenceURI, JsonObject parentDocument) {
        checkArgument(referenceURI.isAbsolute(), "Reference URI must be absolute");
        checkArgument(documentURI.isAbsolute(), "Document URI must be absolute");
        checkNotNull(parentDocument, "parentDocument must not be null");

        //Remove any fragments from the parentDocument URI
        documentURI = URIUtils.withoutFragment(documentURI);

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
