package io.dugnutt.jsonschema.loader;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import io.dugnutt.jsonschema.loader.reference.DefaultSchemaClient;
import io.dugnutt.jsonschema.loader.reference.SchemaCache;
import io.dugnutt.jsonschema.loader.reference.SchemaClient;
import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.SchemaFactory;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.six.SchemaUtils;
import io.dugnutt.jsonschema.six.URIUtils;
import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.loader.ArrayKeywordsLoader.arrayKeywordsLoader;
import static io.dugnutt.jsonschema.loader.NumberKeywordsLoader.numberKeywordsLoader;
import static io.dugnutt.jsonschema.loader.ObjectKeywordsLoader.objectKeywordsLoader;
import static io.dugnutt.jsonschema.loader.SharedKeywordsLoader.sharedKeywordsLoader;
import static io.dugnutt.jsonschema.loader.StringKeywordsLoader.stringKeywordsLoader;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.SchemaLocation.DUGNUTT_UUID_SCHEME;

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
    private final Set<KeywordsLoader> keywordLoaders;

    @Builder
    public JsonSchemaFactory(JsonProvider provider, SchemaClient httpClient, Charset charset, SchemaCache schemaCache,
                             Set<KeywordsLoader> keywordLoaders) {
        this.provider = MoreObjects.firstNonNull(provider, JsonProvider.provider());
        this.httpClient = MoreObjects.firstNonNull(httpClient, new DefaultSchemaClient());
        this.charset = MoreObjects.firstNonNull(charset, UTF8);
        this.schemaCache = MoreObjects.firstNonNull(schemaCache, SchemaCache.schemaCacheBuilder().build());

        Set<KeywordsLoader> loaders = new LinkedHashSet<>();

        loaders.add(sharedKeywordsLoader());
        loaders.add(numberKeywordsLoader());
        loaders.add(stringKeywordsLoader());
        loaders.add(objectKeywordsLoader());
        loaders.add(arrayKeywordsLoader());

        if (keywordLoaders != null) {
            loaders.addAll(keywordLoaders);
        }

        this.keywordLoaders = ImmutableSet.copyOf(loaders);


    }

    public Schema createSchema(SchemaLocation location, JsonObject schemaJson) {
        return createSchema(location, schemaJson, schemaJson);
    }

    public Schema createSchema(SchemaLocation location, JsonObject schemaJson, JsonObject rootSchemaJson) {
        return schemaCache.getSchema(location)
                .orElseGet(() -> {
                    Schema schema = createSchemaBuilder(location, schemaJson, rootSchemaJson).build();
                    schemaCache.cacheSchema(location, schema);
                    return schema;
                });
    }

    public JsonSchemaBuilder createSchemaBuilder(SchemaLocation location, JsonObject schemaJson, JsonObject rootSchemaJson) {
        return createSchemaBuilder(schemaJson, location)
                .location(location)
                .currentDocument(rootSchemaJson)
                .schemaFactory(this);
    }

    public JsonSchemaBuilder createSchemaBuilder(JsonValue value, SchemaLocation path) {
        checkNotNull(value, "value must not be null");
        checkNotNull(path, "path must not be null");
        final JsonValueWithLocation pathAwareJsonValue = JsonValueWithLocation.fromJsonValue(value, path);
        return createSchemaBuilder(pathAwareJsonValue);
    }

    public JsonSchemaBuilder createSchemaBuilder(JsonValueWithLocation schemaJson) {

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

        for (KeywordsLoader keywordLoader : keywordLoaders) {
            keywordLoader.appendKeywords(schemaJson, schemaBuilder, this);
        }

        return schemaBuilder;
    }

    @Override
    public Schema loadRefSchema(Schema referencedFrom, URI refURI, @Nullable JsonObject currentDocument) {
        // Cache ahead to deal with any infinite recursion.
        final SchemaLocation currentLocation = referencedFrom.getLocation();
        schemaCache.cacheSchema(currentLocation.getAbsoluteURI(), referencedFrom);

        // Make sure we're dealing with an absolute URI
        final URI absoluteReferenceURI = currentLocation.getResolutionScope().resolve(refURI);
        final URI documentURI = currentLocation.getDocumentURI();

        // Look for a cache schema at this URI
        final Optional<Schema> cachedSchema = schemaCache.getSchema(absoluteReferenceURI);
        if (cachedSchema.isPresent()) {
            return cachedSchema.get();
        }

        final JsonSchemaBuilder schemaBuilder = findRefInDocument(documentURI, absoluteReferenceURI, currentDocument)
                .orElseGet(() -> findRefInRemoteDocument(absoluteReferenceURI));
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
        final SchemaLocation schemaLocation;
        if (schemaJson.containsKey($ID.key())) {
            String $id = schemaJson.getString($ID.key());
            schemaLocation = SchemaLocation.schemaLocation($id);
        } else {
            schemaLocation = SchemaLocation.anonymousRoot();
        }

        schemaCache.cacheDocument(schemaLocation.getAbsoluteURI(), schemaJson);
        return createSchema(schemaLocation, schemaJson);
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

    JsonSchemaBuilder findRefInRemoteDocument(URI referenceURI) {
        checkNotNull(referenceURI, "referenceURI must not be null");
        URI remoteDocumentURI = referenceURI.resolve("#");
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

            return Optional.of(this.createSchemaBuilder(fetchedDocumentLocation, schemaObject, parentDocument));
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
