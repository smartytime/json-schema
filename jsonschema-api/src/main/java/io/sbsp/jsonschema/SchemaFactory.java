package io.sbsp.jsonschema;

import io.sbsp.jsonschema.loading.LoadingReport;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.net.URI;
import java.util.Optional;

import static io.sbsp.jsonschema.JsonValueWithLocation.fromJsonValue;

public interface SchemaFactory {

    Schema loadRefSchema(Schema referencedFrom, URI refURI, JsonObject currentDocument, LoadingReport report);
    SchemaBuilder createSchemaBuilder(JsonValueWithLocation schemaJson, LoadingReport loadingReport);
    Optional<Schema> findCachedSchema(URI schemaURI);

    default SchemaBuilder createSchemaBuilder(JsonValue value, SchemaLocation location, LoadingReport loadingReport) {
        final JsonValueWithLocation schemaJson = fromJsonValue(value, location);
        return createSchemaBuilder(schemaJson, loadingReport);
    }
}
