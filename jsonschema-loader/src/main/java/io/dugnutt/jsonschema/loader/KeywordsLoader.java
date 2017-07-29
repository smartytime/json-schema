package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;

public interface KeywordsLoader {
    void appendKeywords(JsonValueWithLocation schemaJson, JsonSchemaBuilder schemaBuilder, JsonSchemaFactory factory);
}
