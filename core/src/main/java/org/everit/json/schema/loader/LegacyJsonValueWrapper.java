package org.everit.json.schema.loader;

import org.everit.json.JsonApi;
import org.everit.jsonschema.api.JsonSchemaType;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class LegacyJsonValueWrapper implements org.everit.json.JsonValue<JsonValue> {
    private final JsonValue wrapped;
    private final LegacyJsonObjectApi legacyApi = new LegacyJsonObjectApi();

    private final JsonSchemaType jsonSchemaType;



    public LegacyJsonValueWrapper(JsonValue wrapped, JsonSchemaType jsonSchemaType) {
        this.wrapped = checkNotNull(wrapped);
        this.jsonSchemaType = checkNotNull(jsonSchemaType);
    }

    @Override
    public JsonSchemaType type() {
        return jsonSchemaType;
    }

    @Override
    public Object raw() {
        return wrapped.value();
    }

    @Override
    public JsonValue unbox() {
        return wrapped;
    }

    @Override
    public JsonApi<?> api() {
        return legacyApi;
    }

    @Override
    public List<String> path() {
        return null;
    }
}
