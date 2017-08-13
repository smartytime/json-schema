package io.sbsp.jsonschema;

import io.sbsp.jsonschema.loading.LoadingReport;

import javax.json.JsonObject;

public interface SchemaBuilder {
    Schema build(SchemaLocation itemsLocation, LoadingReport report);

    Schema build();

    SchemaBuilder withSchemaFactory(SchemaFactory factory);
    SchemaBuilder withCurrentDocument(JsonObject currentDocument);
}
