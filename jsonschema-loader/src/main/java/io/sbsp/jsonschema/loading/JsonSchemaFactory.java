package io.sbsp.jsonschema.loading;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.loading.reference.DefaultSchemaClient;
import io.sbsp.jsonschema.loading.reference.SchemaCache;
import io.sbsp.jsonschema.loading.reference.SchemaClient;
import io.sbsp.jsonschema.utils.URIUtils;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.SchemaLocation.BLANK_URI;
import static io.sbsp.jsonschema.SchemaLocation.ROOT_URI;
import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchemaBuilderWithId;
import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.refSchemaBuilder;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$ID;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$REF;
import static io.sbsp.jsonschema.utils.JsonUtils.extract$IdFromObject;

/**
 * Schema factories are responsible for extracting values from a json-object to produce an immutable {@link Schema}
 * instance.  This also includes looking up any referenced schemas.
 */
@Getter
public class JsonSchemaFactory implements SchemaFactory {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    private final JsonProvider provider;
    private final SchemaClient httpClient;
    private final Charset charset;
    private final SchemaCache schemaCache;
    private final SchemaExtractor schemaKeywordExtractor;

    @Builder
    public JsonSchemaFactory(JsonProvider provider, SchemaClient httpClient, Charset charset, SchemaCache schemaCache,
                             SchemaExtractor extractor) {
        this.provider = MoreObjects.firstNonNull(provider, JsonProvider.provider());
        this.httpClient = MoreObjects.firstNonNull(httpClient, new DefaultSchemaClient());
        this.charset = MoreObjects.firstNonNull(charset, UTF8);
        this.schemaCache = MoreObjects.firstNonNull(schemaCache, SchemaCache.schemaCacheBuilder().build());

        this.schemaKeywordExtractor = MoreObjects.firstNonNull(extractor, SchemaExtractors.flexible());
    }

    public Schema loadRootSchema(SchemaLocation location, JsonObject schemaJson) {
        final LoadingReport report = new LoadingReport();
        final Schema schema = loadRootSchema(location, schemaJson, report);
        if (report.hasErrors()) {
            throw new SchemaLoadingException(location.getJsonPointerFragment(), report, schema);
        }
        return schema;
    }
    private Schema loadRootSchema(SchemaLocation location, JsonObject schemaJson, LoadingReport report) {
        return loadSchema(location, schemaJson, schemaJson, report);
    }

    public Schema loadSchema(SchemaLocation location, JsonObject schemaJson, JsonObject rootSchemaJson, LoadingReport report) {
        return schemaCache.getSchema(location)
                .orElseGet(() -> {
                    Schema schema = createSchemaBuilder(location, schemaJson, rootSchemaJson, report).build();
                    schemaCache.cacheSchema(location, schema);
                    return schema;
                });
    }

    public SchemaBuilder createSchemaBuilder(SchemaLocation location, JsonValue schemaJson, JsonObject rootSchemaJson, LoadingReport report) {
        return createSchemaBuilder(schemaJson, location, report)
                .withCurrentDocument(rootSchemaJson);
    }

    public SchemaBuilder createSchemaBuilder(JsonValue value, SchemaLocation path) {
        checkNotNull(value, "value must not be null");
        checkNotNull(path, "path must not be null");
        final JsonValueWithLocation pathAwareJsonValue = JsonValueWithLocation.fromJsonValue(value, path);
        return createSchemaBuilder(pathAwareJsonValue);
    }

    public JsonSchemaBuilder createSchemaBuilder(JsonValueWithLocation schemaJson) {
        return createSchemaBuilder(schemaJson, new LoadingReport());
    }

    public JsonSchemaBuilder createSchemaBuilder(JsonValueWithLocation schemaJson, LoadingReport report) {
        // ##########################
        // $ref: Overrides everything
        // ##########################

        if (schemaJson.has($REF)) {
            //Ignore all other keywords when encountering a ref
            String ref = schemaJson.getString($REF);
            return refSchemaBuilder(URI.create(ref), schemaJson.getLocation(), this);
        }

        final JsonSchemaBuilder schemaBuilder = schemaJson.findString($ID)
                .map($id -> jsonSchemaBuilderWithId(schemaJson.getLocation(), $id))
                .orElse(jsonSchema(schemaJson.getLocation()))
                .withSchemaFactory(this);

        schemaKeywordExtractor.extractSchema(schemaJson, schemaBuilder, this, report);
        return schemaBuilder;
    }

    @Override
    public Schema loadRefSchema(Schema referencedFrom, URI refURI, @Nullable JsonObject currentDocument, LoadingReport report) {
        // Cache ahead to deal with any infinite recursion.
        final SchemaLocation currentLocation = referencedFrom.getLocation();
        schemaCache.cacheSchema(currentLocation, referencedFrom);

        // Make sure we're dealing with an absolute URI
        final URI absoluteReferenceURI = currentLocation.getResolutionScope().resolve(refURI);
        final URI documentURI = currentLocation.getDocumentURI();

        // Look for a cache schema at this URI
        final Optional<Schema> cachedSchema = schemaCache.getSchema(absoluteReferenceURI);
        if (cachedSchema.isPresent()) {
            return cachedSchema.get();
        }

        final SchemaBuilder schemaBuilder = findRefInDocument(documentURI, absoluteReferenceURI, currentDocument, report)
                .orElseGet(() -> findRefInRemoteDocument(absoluteReferenceURI, report));
        final Schema refSchema = schemaBuilder.build();
        schemaCache.cacheSchema(refSchema.getLocation(), refSchema);
        return refSchema;
    }

    @Override
    public Optional<Schema> findCachedSchema(URI schemaURI) {
        return schemaCache.getSchema(schemaURI);
    }

    public Schema load(JsonObject schemaJson) {
        final LoadingReport report = new LoadingReport();
        final Schema loadedSchema = load(schemaJson, report);
        if (report.hasErrors()) {
            throw new SchemaLoadingException(loadedSchema.getPointerFragmentURI(), report, loadedSchema);
        }
        return loadedSchema;
    }

    public Schema load(JsonObject schemaJson, LoadingReport report) throws SchemaLoadingException {
        checkNotNull(schemaJson, "schemaJson must not be null");
        final SchemaLocation schemaLocation;
        if (schemaJson.containsKey($ID.key())) {
            String $id = schemaJson.getString($ID.key());
            final URI $idURI = URI.create($id);
            if (!$idURI.isAbsolute()) {
                schemaLocation = SchemaLocation.hashedRoot(schemaJson, $idURI);
            } else {
                schemaLocation = SchemaLocation.documentRoot($id);
            }

        } else {
            schemaLocation = SchemaLocation.hashedRoot(schemaJson);
        }

        schemaCache.cacheDocument(schemaLocation.getUniqueURI(), schemaJson);
        return loadRootSchema(schemaLocation, schemaJson, report);
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

    JsonObject loadDocument(URI referenceURI) {
        final URI remoteDocumentURI = URIUtils.withoutFragment(referenceURI);
        final JsonObject targetDocument;

        targetDocument = schemaCache.lookupDocument(remoteDocumentURI)
                .orElseGet(() -> {
                    String scheme = Strings.nullToEmpty(referenceURI.getScheme()).toLowerCase();
                    if (!scheme.startsWith("http")) {
                        throw new SchemaException(referenceURI, "Couldn't resolve ref within document, but can't load non-http scheme: %s", scheme);
                    }

                    // Load document remotely
                    try (InputStream inputStream = httpClient.fetchSchema(remoteDocumentURI)) {
                        final JsonObject jsonObject = provider.createReader(inputStream).readObject();
                        schemaCache.cacheDocument(remoteDocumentURI, jsonObject);
                        return jsonObject;
                    } catch (IOException e) {
                        throw new SchemaException(referenceURI, "Error while fetching document '" + referenceURI + "'");
                    }
                });

        if (targetDocument == null) {
            throw new SchemaException(referenceURI, "Unable to get document: " + referenceURI);
        }

        return targetDocument;
    }

    SchemaBuilder findRefInRemoteDocument(URI referenceURI, LoadingReport report) {
        checkNotNull(referenceURI, "referenceURI must not be null");
        URI remoteDocumentURI = referenceURI.resolve("#");
        final JsonObject remoteDocument = loadDocument(remoteDocumentURI);
        return findRefInDocument(remoteDocumentURI, referenceURI, remoteDocument, report)
                .orElseThrow(() -> new SchemaException(referenceURI, "Unable to locate fragment: \n\tFragment: '#%s' in document\n\tDocument:'%s'", referenceURI.getFragment(), remoteDocument));
    }

    Optional<SchemaBuilder> findRefInDocument(URI documentURI, URI referenceURI, JsonObject parentDocument, LoadingReport report) {
        if (parentDocument == null) {
            parentDocument = loadDocument(referenceURI);
        }

        //Remove any fragments from the parentDocument URI
        documentURI = URIUtils.withoutFragment(documentURI);

        // Relativizing strips the path down to only the difference between the documentURI and referenceURI.
        // This will tell us whether the referenceURI is naturally scoped within the parentDocument.
        URI relativeURL = documentURI.relativize(referenceURI);

        final JsonPath pathWithinDocument;
        if (relativeURL.equals(ROOT_URI) || relativeURL.equals(BLANK_URI)) {
            // The parentDocument is the target
            pathWithinDocument = JsonPath.rootPath();
        } else if (URIUtils.isJsonPointer(relativeURL)) {
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

            final URI found$ID = extract$IdFromObject(schemaObject);
            SchemaLocation fetchedDocumentLocation = SchemaLocation.refLocation(documentURI, found$ID, pathWithinDocument);
            return Optional.of(this.createSchemaBuilder(fetchedDocumentLocation, schemaObject, parentDocument, report));
        }
        return Optional.empty();
    }

    public static JsonSchemaFactory schemaFactory() {
        return builder().build();
    }

    public static JsonSchemaFactory schemaFactory(JsonProvider jsonProvider) {
        return JsonSchemaFactory.builder()
                .provider(jsonProvider)
                .build();
    }

    public static class JsonSchemaFactoryBuilder {

    }
}
