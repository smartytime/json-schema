package org.martysoft.json;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

public abstract class BaseJsonObject<T> extends BaseJsonStructure<T, String> implements JsonObject {


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
    //todo:ericm Fix this
    public JsonValue getValue(String jsonPointer) {
        return internalMap().get(jsonPointer);
    }

    /**
     * Returns the value type of this JSON value.
     *
     * @return JSON value type
     */
    @Override
    public ValueType getValueType() {
        return ValueType.OBJECT;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.keySet().contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap().containsValue(value);
    }

    @Override
    public JsonValue get(Object key) {
        return internalMap().get(key);
    }


    @Override
    public Collection<JsonValue> values() {
        return internalMap().values();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return internalMap().entrySet();
    }

    private static Supplier<NullPointerException> exceptionSupplier(String name) {
        return () -> new NullPointerException(String.format("No value found at key %s.", name));
    }
}
