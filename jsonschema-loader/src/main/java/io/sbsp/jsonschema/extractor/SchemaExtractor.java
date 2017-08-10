package io.sbsp.jsonschema.extractor;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.JsonValueWithLocation.fromJsonValue;

public interface SchemaExtractor {
    JsonSchemaBuilder extractSchema(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory factory, ExtractionReport report);

    default JsonSchemaBuilder extractSchema(JsonValue schemaJson, SchemaLocation location, JsonSchemaBuilder builder, SchemaFactory factory, ExtractionReport report) {
        final JsonValueWithLocation schemaJsonValue = fromJsonValue(schemaJson, location);
        return extractSchema(schemaJsonValue, builder, factory, report);
    }
}
