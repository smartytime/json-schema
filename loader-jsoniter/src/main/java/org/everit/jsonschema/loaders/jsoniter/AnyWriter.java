package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.any.Any;

public class AnyWriter implements JsonWriter {

    private Any any;

    @Override
    public JsonWriter array() {
        return null;
    }

    @Override
    public JsonWriter endArray() {
        return null;
    }

    @Override
    public JsonWriter object() {
        return null;
    }

    @Override
    public JsonWriter endObject() {
        return null;
    }

    @Override
    public JsonWriter ifFalse(String key, Boolean value) {
        return null;
    }

    @Override
    public <X> JsonWriter ifPresent(String key, X value) {
        return null;
    }

    @Override
    public JsonWriter ifTrue(String key, Boolean value) {
        return null;
    }

    @Override
    public JsonWriter key(String key) {
        return null;
    }

    @Override
    public <X> JsonWriter value(X value) {
        return null;
    }
}
