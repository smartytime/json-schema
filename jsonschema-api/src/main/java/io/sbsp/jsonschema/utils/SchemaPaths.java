package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.SchemaLocation.SchemaLocationBuilder;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import java.net.URI;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.utils.URIUtils.generateUniqueURI;
import static io.sbsp.jsonschema.utils.URIUtils.resolve;

/**
 * Helper class for creating schema paths for different situations.
 */
public interface SchemaPaths {

    /**
     * Constructs a SchemaLocation instance given an $id that may not be absolute.  In this case,
     * we prepend a unique URI so we can still take advantage of caching.
     */
    static SchemaLocation from$IdNonAbsolute(URI $id) {
        checkNotNull($id, "$id must not be null");

        if ($id.isAbsolute()) {
            return SchemaLocation.builderFrom$Id($id).build();
        } else {
            final URI uniqueURI = generateUniqueURI(UUID.randomUUID());
            final URI resolvedFromUnique = resolve(uniqueURI, $id);
            final SchemaLocationBuilder locationBuilder = SchemaLocation.builderFrom$Id(resolvedFromUnique);
            if (URIUtils.isJsonPointer($id)) {
                locationBuilder.jsonPath(JsonPath.parseFromURIFragment($id));
            }
            return locationBuilder.build();
        }
    }

    /**
     * This builds a uniquely hashed URI from an object source.  Providing the same object source
     * later, woudl return in the same SchemaLocation being created.
     */
    static SchemaLocation fromNonSchemaSource(Object value) {
        checkNotNull(value, "value must not be null");
        return SchemaLocation.builderFrom$Id(generateUniqueURI(value)).build();
    }

    /**
     * This builds an instance from an absolute $id URI.
     */
    static SchemaLocation from$Id(URI $id) {
        checkArgument($id.isAbsolute(), "$id must be absolute");
        return SchemaLocation.builderFrom$Id($id).build();
    }

    /**
     * This builds an instance from the schema values loaded into a builder.  Another builder with
     * the exact same keyword configuration woudl have the same location.
     */
    static SchemaLocation fromBuilder(SchemaBuilder builder) {
        final URI uniqueURIFromBuilder = generateUniqueURI(builder);
        return SchemaLocation.builderFrom$Id(uniqueURIFromBuilder).build();
    }

    /**
     * This builds a unique instance from a json document.  We look for an $id, and build the location
     * based on that.  If no $id is found, then a location that is unique to the json document will
     * be created.
     */
    static SchemaLocation fromDocument(JsonObject documentJson, String $idKey, String... otherIdKeys) {
        // There are three cases here.

        URI $id = JsonUtils.extract$IdFromObject(documentJson, $idKey, otherIdKeys).orElse(null);
        return fromDocumentWithProvided$Id(documentJson, $id);
    }

    static SchemaLocation fromDocumentWithProvided$Id(JsonObject documentRoot, @Nullable URI $id) {
        checkNotNull(documentRoot, "documentRoot must not be null");

        if ($id == null) {
            return SchemaLocation.builderFrom$Id(generateUniqueURI(documentRoot)).build();
        } else if ($id.isAbsolute()) {
            return SchemaLocation.builderFrom$Id($id).build();
        } else {
            final URI uniqueURI = generateUniqueURI(documentRoot);
            final URI resolvedFromUnique = resolve(uniqueURI, $id);
            final SchemaLocationBuilder locationBuilder = SchemaLocation.builderFrom$Id(resolvedFromUnique);
            if (URIUtils.isJsonPointer($id)) {
                locationBuilder.jsonPath(JsonPath.parseFromURIFragment($id));
            }
            return locationBuilder.build();
        }
    }
}
