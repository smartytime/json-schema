package org.everit.json.schema.loader.orgjson;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.jsonschema.api.JsonSchemaWriter;
import org.everit.jsonschema.api.Schema;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class WrappedJSONPrinter implements JsonSchemaWriter {
    private JSONPrinter jsonPrinter;

    public WrappedJSONPrinter(JSONPrinter jsonPrinter) {
        this.jsonPrinter = jsonPrinter;
    }

    @Override
    public JsonSchemaWriter array() {
        jsonPrinter = jsonPrinter.array();
        return this;
    }

    @Override
    public JsonSchemaWriter endArray() {
        jsonPrinter = jsonPrinter.endArray();
        return this;
    }

    @Override
    public JsonSchemaWriter object() {
        jsonPrinter = jsonPrinter.object();
        return this;
    }

    @Override
    public JsonSchemaWriter endObject() {
        jsonPrinter = jsonPrinter.endObject();
        return this;
    }

    @Override
    public JsonSchemaWriter ifFalse(String key, Boolean value) {
        jsonPrinter.ifFalse(key, value);
        return this;

    }

    @Override
    public <X> JsonSchemaWriter ifPresent(String key, X value) {
        jsonPrinter = jsonPrinter.ifPresent(key, value);
        return this;
    }

    public JsonSchemaWriter ifTrue(String key, Boolean value) {
        jsonPrinter.array();
        return this;
    }

    @Override
    public JsonSchemaWriter key(String key) {
        jsonPrinter = jsonPrinter.key(key);
        return this;
    }

    @Override
    public JsonSchemaWriter printSchemaMap(Map<String, Schema> schemas) {
        Map<String, org.everit.json.schema.Schema> converted = new HashMap<>();
        schemas.forEach((key, schema)-> {
            converted.put(key, new org.everit.json.schema.Schema(schema));
        });
        jsonPrinter.printSchemaMap(converted);
        return this;
    }

    @Override
    public JsonSchemaWriter printPatternMap(Map<Pattern, Schema> schemas) {
        Map<Pattern, org.everit.json.schema.Schema> converted = new HashMap<>();
        schemas.forEach((key, schema)-> {
            converted.put(key, new org.everit.json.schema.Schema(schema));
        });
        jsonPrinter.printSchemaMap(converted);
        return this;

    }

    @Override
    public <X> JsonSchemaWriter value(X value) {
        jsonPrinter = jsonPrinter.value(value);
        return this;
    }

    @Override
    public JsonSchemaWriter start() {
        return this; //No-op
    }
}
