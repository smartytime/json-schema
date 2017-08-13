package io.sbsp.jsonschema.loading.reference;

import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.utils.RecursiveJsonIterator;
import io.sbsp.jsonschema.utils.URIUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$ID;

/**
 * @author erosb
 */
@Builder(builderMethodName = "schemaCacheBuilder")
@AllArgsConstructor
@Slf4j(topic = "schemaCache")
public class SchemaCache {

    @NonNull
    @lombok.Builder.Default
    private final Map<URI, Map<URI, JsonPath>> documentIdRefs = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    private final Map<URI, Schema> absoluteSchemaCache = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    private final Map<URI, JsonObject> absoluteDocumentCache = new HashMap<>();

    public void cacheSchema(URI schemaURI, Schema schema) {
        checkState(schemaURI.isAbsolute(), "Must be an absolute URI");
        absoluteSchemaCache.put(normalizeURI(schemaURI), schema);
    }

    public void cacheDocument(URI documentURI, JsonObject document) {
        checkNotNull(documentURI, "documentURI must not be null");
        checkNotNull(document, "document must not be null");

        if (documentURI.isAbsolute()) {
            absoluteDocumentCache.put(normalizeURI(documentURI), document);
        }
    }

    public Optional<JsonObject> lookupDocument(URI documentURI) {
        checkNotNull(documentURI, "documentURI must not be null");
        return Optional.ofNullable(absoluteDocumentCache.get(normalizeURI(documentURI)));
    }

    public void cacheSchema(SchemaLocation location, Schema schema) {
        URI absoluteLocation = location.getUniqueURI();
        URI jsonPointerLocation = location.getAbsoluteJsonPointerURI();
        this.cacheSchema(absoluteLocation, schema);
        this.cacheSchema(jsonPointerLocation, schema);
    }

    public Optional<Schema> getSchema(SchemaLocation schemaLocation) {
        //A schema can be cached in two places
        return getSchema(schemaLocation.getUniqueURI(), schemaLocation.getCanonicalURI());
    }

    public Optional<Schema> getSchema(URI... schemaURI) {
        for (URI uri : schemaURI) {
            if (uri.isAbsolute()) {
                final URI key = normalizeURI(uri);
                final Schema hit = absoluteSchemaCache.get(key);
                if (hit != null) {
                    return Optional.of(hit);
                }
            }
        }
        return Optional.empty();
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
        return URIUtils.trimEmptyFragment(key);
    }

}
