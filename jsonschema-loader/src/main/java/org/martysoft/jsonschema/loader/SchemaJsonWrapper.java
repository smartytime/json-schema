package org.martysoft.jsonschema.loader;

import org.martysoft.jsonschema.v6.JsonSchemaProperty;
import org.martysoft.jsonschema.v6.MissingExpectedPropertyException;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchemaJsonWrapper implements JsonObject {
    private final JsonObject wrapped;

    public SchemaJsonWrapper(JsonObject wrapped) {
        this.wrapped = checkNotNull(wrapped);
    }

    public Optional<JsonValue> find(JsonSchemaProperty prop) {
        if (wrapped.containsKey(prop.key()) && !wrapped.isNull(prop.key())) {
            return Optional.of(wrapped.get(prop.key()));
        }
        return Optional.empty();
    }

    public Optional<JsonString> findString(JsonSchemaProperty property) {
        if (wrapped.containsKey(property.key()) && !wrapped.isNull(property.key())) {
            return Optional.of(wrapped.getJsonString(property.key()));
        }
        return Optional.empty();
    }

    public Optional<JsonNumber> findNumber(JsonSchemaProperty property) {
        if (wrapped.containsKey(property.key()) && !wrapped.isNull(property.key())) {
            return Optional.of(wrapped.getJsonNumber(property.key()));
        }
        return Optional.empty();
    }

    public Optional<Integer> findInt(JsonSchemaProperty property) {
        if (wrapped.containsKey(property.key()) && !wrapped.isNull(property.key())) {
            return Optional.of(wrapped.getJsonNumber(property.key()).intValue());
        }
        return Optional.empty();
    }

    public Optional<JsonArray> findArray(JsonSchemaProperty property) {
        if (wrapped.containsKey(property.key()) && !wrapped.isNull(property.key())) {
            return Optional.of(wrapped.getJsonArray(property.key()));
        }
        return Optional.empty();
    }

    public Optional<JsonObject> findObject(JsonSchemaProperty property) {
        if (wrapped.containsKey(property.key()) && !wrapped.isNull(property.key())) {
            return Optional.of(wrapped.getJsonObject(property.key()));
        }
        return Optional.empty();
    }

    public Optional<Boolean> findBoolean(JsonSchemaProperty property) {
        if (wrapped.containsKey(property.key()) && !wrapped.isNull(property.key())) {
            return Optional.of(wrapped.getBoolean(property.key()));
        }
        return Optional.empty();
    }

    public JsonString expectString(JsonSchemaProperty property) {
        return findString(property).orElseThrow(()-> new MissingExpectedPropertyException(wrapped, property.key()));
    }

    public JsonNumber expectNumber(JsonSchemaProperty property) {
        return findNumber(property).orElseThrow(()-> new MissingExpectedPropertyException(wrapped, property.key()));
    }

    public JsonObject expectObject(JsonSchemaProperty property) {
        return findObject(property).orElseThrow(()-> new MissingExpectedPropertyException(wrapped, property.key()));
    }

    public JsonArray expectArray(JsonSchemaProperty property) {
        return findArray(property).orElseThrow(()-> new MissingExpectedPropertyException(wrapped, property.key()));
    }

    @Override
    public JsonArray getJsonArray(String name) {
        return wrapped.getJsonArray(name);
    }

    @Override
    public JsonObject getJsonObject(String name) {
        return wrapped.getJsonObject(name);
    }

    @Override
    public JsonNumber getJsonNumber(String name) {
        return wrapped.getJsonNumber(name);
    }

    @Override
    public JsonString getJsonString(String name) {
        return wrapped.getJsonString(name);
    }

    @Override
    public String getString(String name) {
        return wrapped.getString(name);
    }

    @Override
    public String getString(String name, String defaultValue) {
        return wrapped.getString(name, defaultValue);
    }

    @Override
    public int getInt(String name) {
        return wrapped.getInt(name);
    }

    @Override
    public int getInt(String name, int defaultValue) {
        return wrapped.getInt(name, defaultValue);
    }

    @Override
    public boolean getBoolean(String name) {
        return wrapped.getBoolean(name);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        return wrapped.getBoolean(name, defaultValue);
    }

    @Override
    public boolean isNull(String name) {
        return wrapped.isNull(name);
    }

    @Override
    public JsonValue getValue(String jsonPointer) {
        return wrapped.getValue(jsonPointer);
    }

    @Override
    public ValueType getValueType() {
        return wrapped.getValueType();
    }

    @Override
    public JsonObject asJsonObject() {
        return wrapped.asJsonObject();
    }

    @Override
    public JsonArray asJsonArray() {
        return wrapped.asJsonArray();
    }

    public boolean has(JsonSchemaProperty property) {
        return wrapped.containsKey(property.key()) && !wrapped.isNull(property.key());
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return wrapped.equals(o);
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return wrapped.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return wrapped.containsValue(value);
    }

    @Override
    @Deprecated
    public JsonValue get(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalStateException("Only string keys are allowed");
        }
        return wrapped.get(key);
    }

    @Override
    public JsonValue put(String key, JsonValue value) {
        return wrapped.put(key, value);
    }

    @Override
    public JsonValue remove(Object key) {
        return wrapped.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends JsonValue> m) {
        wrapped.putAll(m);
    }

    @Override
    public void clear() {
        wrapped.clear();
    }

    @Override
    public Set<String> keySet() {
        return wrapped.keySet();
    }

    @Override
    public Collection<JsonValue> values() {
        return wrapped.values();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return wrapped.entrySet();
    }

    @Override
    public JsonValue getOrDefault(Object key, JsonValue defaultValue) {
        return wrapped.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super JsonValue> action) {
        wrapped.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super JsonValue, ? extends JsonValue> function) {
        wrapped.replaceAll(function);
    }

    @Override
    public JsonValue putIfAbsent(String key, JsonValue value) {
        return wrapped.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return wrapped.remove(key, value);
    }

    @Override
    public boolean replace(String key, JsonValue oldValue, JsonValue newValue) {
        return wrapped.replace(key, oldValue, newValue);
    }

    @Override
    public JsonValue replace(String key, JsonValue value) {
        return wrapped.replace(key, value);
    }

    @Override
    public JsonValue computeIfAbsent(String key, Function<? super String, ? extends JsonValue> mappingFunction) {
        return wrapped.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public JsonValue computeIfPresent(String key, BiFunction<? super String, ? super JsonValue, ? extends JsonValue> remappingFunction) {
        return wrapped.computeIfPresent(key, remappingFunction);
    }

    @Override
    public JsonValue compute(String key, BiFunction<? super String, ? super JsonValue, ? extends JsonValue> remappingFunction) {
        return wrapped.compute(key, remappingFunction);
    }

    @Override
    public JsonValue merge(String key, JsonValue value, BiFunction<? super JsonValue, ? super JsonValue, ? extends JsonValue> remappingFunction) {
        return wrapped.merge(key, value, remappingFunction);
    }
}
