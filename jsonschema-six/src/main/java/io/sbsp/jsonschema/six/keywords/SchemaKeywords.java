package io.sbsp.jsonschema.six.keywords;

import io.sbsp.jsonschema.six.JsonSchemaGenerator;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;

import java.util.Set;

public interface SchemaKeywords {
    Set<JsonSchemaType> getApplicableTypes();
    JsonSchemaGenerator toJson(JsonSchemaGenerator generator);
}
