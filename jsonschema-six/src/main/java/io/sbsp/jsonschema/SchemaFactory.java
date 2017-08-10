package io.sbsp.jsonschema;

import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.extractor.ExtractionReport;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.net.URI;
import java.util.Optional;

import static io.sbsp.jsonschema.JsonValueWithLocation.fromJsonValue;

public interface SchemaFactory {
    Schema loadRefSchema(Schema referencedFrom, URI refURI, JsonObject currentDocument);
    JsonSchemaBuilder createSchemaBuilder(JsonValueWithLocation schemaJson, ExtractionReport extractionReport);
    Optional<Schema> findCachedSchema(URI schemaURI);

    default JsonSchemaBuilder createSchemaBuilder(JsonValue value, SchemaLocation location, ExtractionReport extractionReport) {
        final JsonValueWithLocation schemaJson = fromJsonValue(value, location);
        return createSchemaBuilder(schemaJson, extractionReport);
    }
}
