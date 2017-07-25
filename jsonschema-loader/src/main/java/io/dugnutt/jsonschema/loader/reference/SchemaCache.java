package io.dugnutt.jsonschema.loader.reference;

import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.RecursiveJsonIterator;
import io.dugnutt.jsonschema.six.SchemaLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;

/**
 * @author erosb
 */
@Builder(builderClassName = "Builder", builderMethodName = "schemaCacheBuilder")
@AllArgsConstructor
public class SchemaCache {

    @NonNull
    @lombok.Builder.Default
    private final Map<URI, Map<URI, JsonPath>> documentIdRefs = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    private final Map<URI, JsonSchema> absoluteSchemaCache = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    private final Map<URI, JsonObject> absoluteDocumentCache = new HashMap<>();



    // @NonNull
    // private final Map<URI, Supplier<Schema>> loadingStack;

    public void cacheDocumentLocalSchema(URI documentURI, URI schemaURI, JsonSchema schema) {
        checkNotNull(schema, "schema must not be null");
        checkNotNull(documentURI, "documentURI must not be null");
        checkNotNull(schemaURI, "schemaURI must not be null");

        getDocumentLocalCache(documentURI).put(schemaURI, schema);
    }

    public void cacheSchema(String schemaURL, JsonSchema schema) {
        this.cacheSchema(URI.create(schemaURL), schema);
    }

    public void cacheSchema(URI schemaURL, JsonSchema schema) {
        absoluteSchemaCache.put(schemaURL, schema);
    }

    public void cacheDocument(URI documentURI, JsonObject document) {
        checkNotNull(documentURI, "documentURI must not be null");
        checkNotNull(document, "document must not be null");
        absoluteDocumentCache.put(documentURI, document);
    }

    public Optional<JsonObject> lookupDocument(URI documentURI) {
        checkNotNull(documentURI, "documentURI must not be null");
        return Optional.ofNullable(absoluteDocumentCache.get(documentURI));
    }

    /**
     * Retrieves a reference or pointer schema for the given loader model.
     *
     * @param location Represents the location in the schema that we're trying to load a reference for
     * @return
     */
    // @Override
    // public ReferenceSchema.Builder createReferenceSchemaBuilder(SchemaLoaderModel forModel) {
    //     try {
    //         checkArgument(forModel.has(JsonSchemaKeyword.$REF));
    //         return this.referenceSchemaCacheByAbsUrl.get(forModel);
    //     } catch (ExecutionException e) {
    //         throw forModel.createSchemaException(e.getMessage());
    //     }
    // }
    public void cacheSchema(SchemaLocation location, JsonSchema schema) {
        URI absoluteLocation = location.getAbsoluteURI();
        URI jsonPointerLocation = location.getAbsoluteJsonPointerURI();
        this.cacheSchema(absoluteLocation, schema);
        this.cacheSchema(jsonPointerLocation, schema);
    }

    public Optional<JsonSchema> getSchema(SchemaLocation schemaLocation) {
        //A schema can be cached in two places
        return getSchema(schemaLocation.getAbsoluteURI(), schemaLocation.getAbsoluteJsonPointerURI());
    }

    public Optional<JsonSchema> getSchema(URI... possibilities) {
        //A schema can be cached a bunch of places.
        return Stream.of(possibilities)
                .filter(absoluteSchemaCache::containsKey)
                .map(absoluteSchemaCache::get)
                .findAny();
    }

    public Optional<JsonSchema> getSchema(URI schemaURI) {
        final JsonSchema cacheHit = absoluteSchemaCache.get(schemaURI);
        return Optional.ofNullable(cacheHit);
    }

    public Optional<JsonPath> resolveURIToDocumentUsingLocalIdentifiers(URI documentURI, URI absoluteURI, JsonObject document) {
        checkNotNull(documentURI, "documentURI must not be null");
        checkNotNull(document, "document must not be null");
        checkNotNull(absoluteURI, "absoluteURI must not be null");

        final JsonPath pathForURI = documentIdRefs.computeIfAbsent(documentURI, key -> {
            Map<URI, JsonPath> values = new HashMap<>();
            RecursiveJsonIterator.visitDocument(document, (keyOrIndex, val, path) -> {
                if ($ID.key().equals(keyOrIndex)) {
                    if (val.getValueType() == JsonValue.ValueType.STRING) {
                        final URI $idAsURI = URI.create(((JsonString) val).getString());
                        URI absoluteIdentifier = documentURI.resolve($idAsURI);
                        values.put(absoluteIdentifier, path);
                    }

                }
            });

            return values;
        }).get(absoluteURI);

        return Optional.ofNullable(pathForURI);
    }

    // JsonObject withoutRef(JsonObject original) {
    //     JsonObjectBuilder b = schemaFactory.getProvider().createObjectBuilder(original);
    //     b.remove(JsonSchemaKeyword.$REF.key());
    //
    //     //todo:ericm Need a path here??
    //     return b.build();
    // }

    private Map<URI, JsonSchema> getDocumentLocalCache(URI documentURI) {
        // return documentIdRefs.computeIfAbsent(documentURI, u -> new HashMap<>());
        return null;
    }

    // private ReferenceSchema.Builder internalCreateReferenceSchemaBuilder(SchemaLoaderModel forModel) {
    //     SchemaJsonObject refSchemaJson = forModel.getSchemaJson();
    //     String referenceURI = refSchemaJson.getString(JsonSchemaKeyword.$REF);
    //
    //     String absoluteUrl = ReferenceScopeResolver.resolveScope(forModel.getId(), referenceURI).toString();
    //     if (schemaFactory.get.pointerSchemas.containsKey(absPointerString)) {
    //         return schemaLoaderModel.pointerSchemas.get(absPointerString);
    //     }
    //     boolean isExternal = !absPointerString.startsWith("#");
    //     JsonPointerResolver pointer = isExternal
    //             ? JsonPointerResolver.forURL(schemaLoaderModel.httpClient, absPointerString, provider)
    //             : JsonPointerResolver.forDocument(schemaLoaderModel.rootSchemaJson, absPointerString, provider);
    //     ReferenceSchema.Builder refBuilder = ReferenceSchema.stringKeywordsBuilder()
    //             .refValue(jsonPointerVal);
    //     schemaLoaderModel.pointerSchemas.put(absPointerString, refBuilder);
    //
    //     JsonPointerResolver.QueryResult queryResult = pointer.query();
    //     //We shouldn't do this...
    //     // JsonObject resultObject = combineWithRefSchema(provider, withoutRef(document), queryResult.getQueryResult());
    //     JsonObject resultObject = queryResult.getQueryResult();
    //     SchemaLoader childLoader = schemaLoaderModel.initChildLoader()
    //             .resolutionScope(isExternal ? withoutFragment(absPointerString) : schemaLoaderModel.id)
    //             .schemaJson(new SchemaJsonObject(resultObject, path))
    //             .rootSchemaJson(queryResult.getContainingDocument()).build();
    //     Schema referredSchema = childLoader.load().build();
    //     refBuilder.build().setReferredSchema(referredSchema);
    //
    //     // Hmmmmm...
    //     try (InputStream schemaStream = schemaClient.get()) {
    //         JsonObject referenceSchema = schemaFactory.getProvider().createReader(schemaStream).readObject();
    //         ReferenceSchema.stringKeywordsBuilder().refValue(model).
    //     }
    //
    //     return refBuilder;
    // }

    private static JsonObject combineWithRefSchema(JsonProvider provider, JsonObject additional, JsonObject original) {
        if (additional.keySet().isEmpty()) {
            return original;
        }
        if (original.keySet().isEmpty()) {
            return additional;
        }
        JsonObjectBuilder b = provider.createObjectBuilder();

        original.forEach(b::add);
        additional.forEach(b::add);
        return b.build();
    }

    public static class Builder {

        public Builder cacheSchema(URI key, JsonSchema toCache) {
            checkNotNull(key, "key must not be null");
            checkNotNull(toCache, "toCache must not be null");
            this.cacheSchema(key, toCache);
            return this;
        }
    }
}
