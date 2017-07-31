package io.dugnutt.jsonschema.six.keywords;

import io.dugnutt.jsonschema.six.JsonSchemaGenerator;
import io.dugnutt.jsonschema.six.enums.JsonSchemaType;

import java.util.Set;

public interface SchemaKeywords {
    Set<JsonSchemaType> getApplicableTypes();
    JsonSchemaGenerator toJson(JsonSchemaGenerator generator);
}
