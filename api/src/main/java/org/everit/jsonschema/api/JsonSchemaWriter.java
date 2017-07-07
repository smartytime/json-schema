package org.everit.jsonschema.api;

import java.util.Map;
import java.util.regex.Pattern;

public interface JsonSchemaWriter {

    JsonSchemaWriter array();

    JsonSchemaWriter endArray();

    JsonSchemaWriter object();

    JsonSchemaWriter endObject();

    <X> JsonSchemaWriter ifFalse(String key, X value);

    <X> JsonSchemaWriter ifPresent(String key, X value);

    <X> JsonSchemaWriter ifTrue(String key, X value);

    JsonSchemaWriter key(String key);

    JsonSchemaWriter printSchemaMap(Map<String, Schema> schemas);

    JsonSchemaWriter printPatternMap(Map<Pattern, Schema> schemas);

    <X> JsonSchemaWriter value(X value);

    JsonSchemaWriter start();
}
