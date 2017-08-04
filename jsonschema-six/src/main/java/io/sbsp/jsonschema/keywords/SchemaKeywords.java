package io.sbsp.jsonschema.keywords;

import io.sbsp.jsonschema.enums.JsonSchemaType;

import javax.json.stream.JsonGenerator;
import java.util.Set;

public interface SchemaKeywords {
    Set<JsonSchemaType> getApplicableTypes();
    JsonGenerator toJson(JsonGenerator generator);
}
