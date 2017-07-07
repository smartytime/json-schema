package org.everit.json.schema.loader;

import org.everit.json.*;
import org.everit.jsonschema.api.JsonSchemaWriter;

import java.util.List;
import java.util.Map;

public class LegacyJsonObjectApi implements JsonApi<JsonValue> {
    @Override
    public org.everit.json.JsonObject readJson(String jsonValue) {
        return null;
    }

    @Override
    public JsonSchemaWriter getWriter() {
        return null;
    }

    @Override
    public org.everit.json.JsonValue<?> of(JsonValue raw) {
        return null;
    }

    @Override
    public JsonPointer pointer(List<String> path) {
        return null;
    }

    @Override
    public JsonPointer pointer(String... path) {
        return null;
    }

    @Override
    public org.everit.json.JsonObject fromMap(Map<String, Object> map) {
        return null;
    }

    @Override
    public void handleException(Exception e) {

    }
}
