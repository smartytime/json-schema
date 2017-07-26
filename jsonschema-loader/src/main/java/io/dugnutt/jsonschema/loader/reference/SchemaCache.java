package io.dugnutt.jsonschema.loader.reference;

import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchemaDetails;
import io.dugnutt.jsonschema.six.RecursiveJsonIterator;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.six.URIUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
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
    private final Map<URI, Schema> absoluteSchemaCache = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    private final Map<URI, JsonSchemaDetails> absoluteSchemaDetails = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    private final Map<URI, JsonObject> absoluteDocumentCache = new HashMap<>();

    // @NonNull
    // private final Map<URI, Supplier<Schema>> loadingStack;

    public void cacheSchema(URI schemaURI, Schema schema) {
        absoluteSchemaCache.put(normalizeURI(schemaURI), schema);
    }

    public void cacheDocument(URI documentURI, JsonObject document) {
        checkNotNull(documentURI, "documentURI must not be null");
        checkNotNull(document, "document must not be null");
        absoluteDocumentCache.put(normalizeURI(documentURI), document);
    }

    public Optional<JsonObject> lookupDocument(URI documentURI) {
        checkNotNull(documentURI, "documentURI must not be null");
        return Optional.ofNullable(absoluteDocumentCache.get(normalizeURI(documentURI)));
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
    public void cacheSchema(SchemaLocation location, Schema schema) {
        URI absoluteLocation = location.getAbsoluteURI();
        URI jsonPointerLocation = location.getAbsoluteJsonPointerURI();
        this.cacheSchema(absoluteLocation, schema);
        this.cacheSchema(jsonPointerLocation, schema);
    }

    public Optional<Schema> getSchema(SchemaLocation schemaLocation) {
        //A schema can be cached in two places
        return getSchema(schemaLocation.getAbsoluteURI(), schemaLocation.getAbsoluteJsonPointerURI());
    }

    public Optional<Schema> getSchema(URI... possibilities) {
        //A schema can be cached a bunch of places.
        return Stream.of(possibilities)
                .map(this::normalizeURI)
                .filter(absoluteSchemaCache::containsKey)
                .map(absoluteSchemaCache::get)
                .findAny();
    }

    public Optional<Schema> getSchema(URI schemaURI) {
        final Schema cacheHit = absoluteSchemaCache.get(normalizeURI(schemaURI));
        return Optional.ofNullable(cacheHit);
    }

    public Optional<JsonPath> resolveURIToDocumentUsingLocalIdentifiers(URI documentURI, URI absoluteURI, JsonObject document) {
        checkNotNull(documentURI, "documentURI must not be null");
        checkNotNull(document, "document must not be null");
        checkNotNull(absoluteURI, "absoluteURI must not be null");

        final URI normalizedDocumentURI = normalizeURI(documentURI);
        final URI normalizedAbsoluteURI = normalizeURI(absoluteURI);

        final JsonPath pathForURI = documentIdRefs.computeIfAbsent(normalizedDocumentURI, key -> {
            Map<URI, JsonPath> values = new HashMap<>();
            RecursiveJsonIterator.visitDocument(document, (keyOrIndex, val, path) -> {
                if ($ID.key().equals(keyOrIndex)) {
                    if (val.getValueType() == JsonValue.ValueType.STRING) {
                        final URI $idAsURI = normalizeURI(URI.create(((JsonString) val).getString()));
                        URI absoluteIdentifier = normalizedDocumentURI.resolve($idAsURI);
                        values.put(absoluteIdentifier, path);
                    }
                }
            });

            return values;
        }).get(normalizedAbsoluteURI);

        return Optional.ofNullable(pathForURI);
    }

    private URI normalizeURI(URI key) {
        return URIUtils.withoutFragment(key);
    }

    public static class Builder {
        public Builder cacheSchema(URI key, Schema toCache) {
            checkNotNull(key, "key must not be null");
            checkNotNull(toCache, "toCache must not be null");
            this.cacheSchema(key, toCache);
            return this;
        }
    }
}
