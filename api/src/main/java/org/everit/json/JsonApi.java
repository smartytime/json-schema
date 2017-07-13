package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.JsonSchemaWriter;

import java.util.Map;

public interface JsonApi<X> {

    JsonObject<X> readJson(String jsonValue);

    JsonSchemaWriter getWriter();

    JsonSchemaType schemaType(X x);

    // boolean isNull(X subject);

    JsonValue<?> of(X raw, JsonPath path);

    JsonPointer pointer(JsonPath path);

    JsonObject<?> fromMap(Map<String, Object> map, JsonPath path);
}
