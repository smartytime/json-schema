package org.everit.jsonschema.loaders.jsoniter;

import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.api.JsonSchemaWriter;

import java.util.Map;
import java.util.regex.Pattern;

public class AnyWriter implements JsonSchemaWriter {
    @Override
    public JsonSchemaWriter array() {
        return null;
    }

    @Override
    public JsonSchemaWriter endArray() {
        return null;
    }

    @Override
    public JsonSchemaWriter object() {
        return null;
    }

    @Override
    public JsonSchemaWriter endObject() {
        return null;
    }

    @Override
    public <X> JsonSchemaWriter ifFalse(String key, X value) {
        return null;
    }

    @Override
    public <X> JsonSchemaWriter ifPresent(String key, X value) {
        return null;
    }

    @Override
    public <X> JsonSchemaWriter ifTrue(String key, X value) {
        return null;
    }

    @Override
    public JsonSchemaWriter key(String key) {
        return null;
    }

    @Override
    public JsonSchemaWriter printSchemaMap(Map<String, Schema> schemas) {
        return null;
    }

    @Override
    public JsonSchemaWriter printPatternMap(Map<Pattern, Schema> schemas) {
        return null;
    }

    @Override
    public <X> JsonSchemaWriter value(X value) {
        return null;
    }

    @Override
    public JsonSchemaWriter start() {
        return null;
    }
}
