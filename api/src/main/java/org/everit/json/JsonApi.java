package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaWriter;

import java.util.List;
import java.util.Map;

public interface JsonApi<X> {
    JsonObject readJson(String jsonValue);

    JsonSchemaWriter getWriter();

    // boolean isNull(X subject);

    JsonValue of(X raw);

    JsonPointer pointer(List<String> path);
    JsonPointer pointer(String... path);

    JsonObject fromMap(Map<String, Object> map);

    void handleException(Exception e);

}
