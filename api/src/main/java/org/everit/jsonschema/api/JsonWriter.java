package org.everit.jsonschema.api;

/**
 * Interface for writing out json.  Used so elements can serialize their options back into a valid json-schema.
 */
public interface JsonWriter {

    JsonWriter array();

    JsonWriter endArray();

    JsonWriter object();

    JsonWriter endObject();

    JsonWriter ifFalse(String key, Boolean value);

    <X> JsonWriter ifPresent(String key, X value);

    JsonWriter ifTrue(String key, Boolean value);

    JsonWriter key(String key);

    <X> JsonWriter value(X value);
}
