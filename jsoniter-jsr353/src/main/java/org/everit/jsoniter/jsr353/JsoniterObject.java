package org.everit.jsoniter.jsr353;

import com.jsoniter.any.Any;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JsoniterObject extends JsoniterStructure<String> implements JsonObject {

    public JsoniterObject(Any wrapped) {
        super(wrapped);
    }

    @Override
    public Stream<String> cacheKeyStream() {
        return wrapped.keys().stream();
    }

    @Override
    public JsonArray getJsonArray(String name) {
        return find(name, JsonArray.class).orElse(null);
    }

    @Override
    public JsonObject getJsonObject(String name) {
        return find(name, JsonObject.class).orElse(null);
    }

    @Override
    public JsonNumber getJsonNumber(String name) {
        return find(name, JsonNumber.class).orElse(null);
    }

    @Override
    public JsonString getJsonString(String name) {
        return find(name, JsonString.class).orElse(null);
    }

    @Override
    public String getString(String name) {
        return findString(name).orElseThrow(exceptionSupplier(name));
    }

    @Override
    public String getString(String name, String defaultValue) {
        return findString(name).orElse(defaultValue);
    }

    @Override
    public int getInt(String name) {
        return findNumber(name)
                .map(Number::intValue)
                .orElseThrow(exceptionSupplier(name));
    }

    @Override
    public int getInt(String name, int defaultValue) {
        return findNumber(name)
                .map(Number::intValue)
                .orElse(defaultValue);
    }

    @Override
    public boolean getBoolean(String name) {
        return findBoolean(name).orElseThrow(exceptionSupplier(name));
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        return findBoolean(name).orElse(defaultValue);
    }

    @Override
    public boolean isNull(String name) {
        return wrapped.get(name).valueType() == com.jsoniter.ValueType.NULL;
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return wrapped.keys().contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internal().containsValue(value);
    }

    @Override
    public JsonValue get(Object key) {
        return internal().get(key);
    }

    @Override
    public JsonValue put(String key, JsonValue value) {
        throw new UnsupportedOperationException("Jsoniter is read-only");
    }

    @Override
    public JsonValue remove(Object key) {
        throw new UnsupportedOperationException("Jsoniter is read-only");
    }

    @Override
    public void putAll(Map<? extends String, ? extends JsonValue> m) {
        throw new UnsupportedOperationException("Jsoniter is read-only");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Jsoniter is read-only");
    }

    @Override
    public Set<String> keySet() {
        return wrapped.keys();
    }

    @Override
    public Collection<JsonValue> values() {
        return internal().values();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return internal().entrySet();
    }

    private static Supplier<NullPointerException> exceptionSupplier(String name) {
        return () -> new NullPointerException(String.format("No value found at key %s.", name));
    }
}
