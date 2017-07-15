package org.everit.json;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonObjectImpl implements ImmutableJsonObject {

    private final Map<String, JsonValue> internal;

    public JsonObjectImpl(Map<String, JsonValue> values) {
        this.internal = checkNotNull(values);
    }

    @Override
    public JsonArray getJsonArray(String name) {
        return internal.get(name).asJsonArray();
    }

    @Override
    public JsonObject getJsonObject(String name) {
        return internal.get(name).asJsonObject();
    }

    @Override
    public JsonNumber getJsonNumber(String name) {
        return (JsonNumber) internal.get(name);
    }

    @Override
    public JsonString getJsonString(String name) {
        return (JsonString) internal.get(name);
    }

    @Override
    public String getString(String name) {
        return getJsonString(name).getString();
    }

    @Override
    public String getString(String name, String defaultValue) {
        JsonString jsonString = getJsonString(name);
        return jsonString != null ? jsonString.getString() : defaultValue;
    }

    @Override
    public int getInt(String name) {
        return getJsonNumber(name).intValue();
    }

    @Override
    public int getInt(String name, int defaultValue) {
        JsonNumber jsonNumber = getJsonNumber(name);
        return jsonNumber != null ? jsonNumber.intValue() : defaultValue;
    }

    @Override
    public boolean getBoolean(String name) {
        JsonValue jsonValue = internal.get(name);
        if (jsonValue == JsonValue.TRUE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        JsonValue jsonValue = internal.get(name);
        if (jsonValue == JsonValue.TRUE) {
            return true;
        } else if (jsonValue == FALSE) {
            return false;
        } else {
            return defaultValue;
        }
    }

    @Override
    public boolean isNull(String name) {
        return get(name) == null || get(name) == NULL;
    }

    @Override
    public int size() {
        return internal.size();
    }

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internal.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internal.containsValue(value);
    }

    @Override
    public JsonValue get(Object key) {
        return internal.get(key);
    }

    @Override
    public Set<String> keySet() {
        return internal.keySet();
    }

    @Override
    public Collection<JsonValue> values() {
        return internal.values();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return internal.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super JsonValue> action) {
        internal.forEach(action);
    }
}
