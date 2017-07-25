package io.dugnutt.jsonschema.six;

import java.util.Set;

public interface SchemaKeywords {
    Set<JsonSchemaType> getApplicableTypes();
    JsonSchemaGenerator toJson(JsonSchemaGenerator generator);
}
