package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema.JsonSchemaBuilder;

public interface KeywordsLoader {
    void appendKeywords(JsonValueWithLocation schemaJson, JsonSchemaBuilder schemaBuilder, JsonSchemaFactory factory);
}
