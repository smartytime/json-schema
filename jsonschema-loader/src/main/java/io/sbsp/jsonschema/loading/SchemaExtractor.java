package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.JsonValueWithLocation.fromJsonValue;

public interface SchemaExtractor {
    JsonSchemaBuilder extractSchema(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory factory, LoadingReport report);

    default JsonSchemaBuilder extractSchema(JsonValue schemaJson, SchemaLocation location, JsonSchemaBuilder builder, SchemaFactory factory, LoadingReport report) {
        final JsonValueWithLocation schemaJsonValue = fromJsonValue(schemaJson, location);
        return extractSchema(schemaJsonValue, builder, factory, report);
    }
}
