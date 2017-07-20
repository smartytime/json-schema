package io.dugnutt.jsonschema;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface PartialJsonObject extends JsonObject {
    @Override
    @Deprecated
    default JsonArray getJsonArray(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonObject getJsonObject(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonNumber getJsonNumber(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonString getJsonString(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default String getString(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default String getString(String name, String defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default int getInt(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default int getInt(String name, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean getBoolean(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean getBoolean(String name, boolean defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean isNull(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default ValueType getValueType() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonValue get(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonValue put(String key, JsonValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonValue remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void putAll(Map<? extends String, ? extends JsonValue> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default Collection<JsonValue> values() {
        throw new UnsupportedOperationException();
    }

}
