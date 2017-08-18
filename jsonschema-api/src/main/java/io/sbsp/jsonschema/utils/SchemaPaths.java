package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.SchemaLocation.SchemaLocationBuilder;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.net.URI;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.utils.URIUtils.generateUniqueURI;
import static io.sbsp.jsonschema.utils.URIUtils.resolve;

public interface SchemaPaths {

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

    static SchemaLocation fromNonSchemaSource(Object value) {
        checkNotNull(value, "value must not be null");
        return SchemaLocation.builderFrom$Id(generateUniqueURI(value)).build();
    }



    static SchemaLocation from$Id(URI $id) {
        checkArgument($id.isAbsolute(), "$id must be absolute");
        return SchemaLocation.builderFrom$Id($id).build();
    }

    static SchemaLocation fromBuilder(SchemaBuilder builder) {
        final URI uniqueURIFromBuilder = generateUniqueURI(builder);
        return SchemaLocation.builderFrom$Id(uniqueURIFromBuilder).build();
    }

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
