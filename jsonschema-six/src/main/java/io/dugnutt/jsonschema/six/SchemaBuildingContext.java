package io.dugnutt.jsonschema.six;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;

public class SchemaBuildingContext {
    private final Map<URI, Schema.JsonSchemaBuilder> buildingCache = new HashMap<>();
    private final Map<URI, JsonSchema> schemaCache = new HashMap<>();

    public void cacheBuilder(URI uri, JsonSchemaBuilder builder) {
        buildingCache.putIfAbsent(uri, builder);
    }

    public void cacheSchema(URI uri, JsonSchema schema) {
        schemaCache.putIfAbsent(uri, schema);
    }

    public Optional<JsonSchemaBuilder> findBuilder(URI uri) {
        return Optional.ofNullable(buildingCache.get(uri));
    }

    public Optional<JsonSchema> findSchema(URI uri) {
        return Optional.ofNullable(schemaCache.get(uri));
    }
}
