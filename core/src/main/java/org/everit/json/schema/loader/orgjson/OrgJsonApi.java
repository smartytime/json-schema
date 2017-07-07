package org.everit.json.schema.loader.orgjson;

import org.everit.json.JsonApi;
import org.everit.json.JsonObject;
import org.everit.json.JsonPointer;
import org.everit.json.JsonValue;
import org.everit.jsonschema.api.JsonSchemaWriter;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class OrgJsonApi implements JsonApi<JSONObject> {
    @Override
    public JsonObject readJson(String jsonValue) {
        return null;
    }

    @Override
    public JsonSchemaWriter getWriter() {
        return null;
    }

    @Override
    public JsonValue of(JSONObject raw) {
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
    public JsonObject fromMap(Map<String, Object> map) {
        return null;
    }

    @Override
    public void handleException(Exception e) {

    }
}
