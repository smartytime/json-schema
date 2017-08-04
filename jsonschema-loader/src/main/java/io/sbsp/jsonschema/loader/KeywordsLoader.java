package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema.JsonSchemaBuilder;

public interface KeywordsLoader {
    void appendKeywords(JsonValueWithLocation schemaJson, JsonSchemaBuilder schemaBuilder, JsonSchemaFactory factory);
}
