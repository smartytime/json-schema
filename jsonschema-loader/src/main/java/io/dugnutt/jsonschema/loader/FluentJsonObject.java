package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonPointerPath;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.MissingExpectedPropertyException;
import io.dugnutt.jsonschema.six.UnexpectedValueException;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.loader.LoadingUtils.castTo;

public class FluentJsonObject implements JsonObject {
    private final JsonObject wrapped;

    //Used to attach to error messages.
    private final JsonPointerPath path;

    public FluentJsonObject(JsonObject wrapped, JsonPointerPath path) {
        this.wrapped = checkNotNull(wrapped);
        this.path = path;
    }

    public JsonArray expectArray(JsonSchemaKeyword property) {
        return findArray(property).orElseThrow(() -> new MissingExpectedPropertyException(wrapped, property.key()));
    }

    public Number expectNumber(JsonSchemaKeyword property) {
        return findNumber(property).orElseThrow(() -> new MissingExpectedPropertyException(wrapped, property.key()));
    }

    public JsonObject expectObject(JsonSchemaKeyword property) {
        return findObject(property).orElseThrow(() -> new MissingExpectedPropertyException(wrapped, property.key()));
    }

    public String expectString(JsonSchemaKeyword property) {
        return findString(property).orElseThrow(() -> new MissingExpectedPropertyException(wrapped, property.key()));
    }

    public Optional<JsonValue> findByKey(JsonSchemaKeyword prop) {
        if (wrapped.containsKey(prop.key())) {
            return Optional.of(wrapped.get(prop.key()));
        }
        return Optional.empty();
    }

    public Optional<JsonArray> findArray(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonArray.class);
    }

    public Optional<Boolean> findBoolean(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        if (wrapped.containsKey(property.key()) && !wrapped.isNull(property.key())) {
            try {
                return Optional.of(wrapped.getBoolean(property.key()));
            } catch (ClassCastException e) {
                throw new UnexpectedValueException(path, wrapped.get(property.key()), ValueType.TRUE, ValueType.FALSE);
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> findInt(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonNumber.class)
                .map(JsonNumber::intValue);
    }

    public Optional<Integer> findInteger(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonNumber.class)
                .map(JsonNumber::intValue);
    }

    public Optional<Number> findNumber(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonNumber.class)
                .map(JsonNumber::bigDecimalValue);
    }

    public Optional<JsonObject> findObject(String property) {
        checkNotNull(property, "property must not be null");
        return findByKey(property, JsonObject.class);
    }

    public Optional<JsonObject> findIfObject(String property) {
        checkNotNull(property, "property must not be null");
        return findByKey(property, JsonStructure.class)
                .filter(childJson-> JsonObject.class.isAssignableFrom(childJson.getClass()))
                .map(JsonObject.class::cast);
    }

    public Optional<JsonObject> findObject(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");
        return findObject(property.key());
    }

    public Optional<String> findString(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");
        return findByKey(property.key(), JsonString.class)
                .map(JsonString::getString);
    }

    public <X extends JsonStructure> Optional<X> findStructure(JsonSchemaKeyword keyword, Class<X> expectedClass) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(expectedClass, "expectedClass must not be null");

        final String keywordVal = keyword.key();
        final JsonPointerPath childPath = path.child(keywordVal);

        return this.findByKey(keyword)
                .map(castTo(JsonStructure.class, childPath.toURIFragment())) //Hard cast - will error if it's not a structure
                .filter(value -> expectedClass.isAssignableFrom(value.getClass()))
                .map(expectedClass::cast);
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

    public JsonObject getJsonObject(JsonSchemaKeyword name) {
        try {
            return wrapped.getJsonObject(name.key());
        } catch (ClassCastException e) {
            throw new UnexpectedValueException(path, wrapped.get(name.key()), ValueType.OBJECT);
        }
    }

    public String getString(JsonSchemaKeyword property) {
        try {
            if (wrapped.isNull(property.key())) {
                return null;
            }
            return wrapped.getString(property.key());
        } catch (ClassCastException e) {
            throw new UnexpectedValueException(path, wrapped.get(property.key()), ValueType.STRING);
        }
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

    public boolean has(JsonSchemaKeyword property) {
        return wrapped.containsKey(property.key());
    }

    public boolean hasAny(JsonSchemaKeyword... property) {
        for (JsonSchemaKeyword jsonSchemaKeyword : property) {
            if (wrapped.containsKey(jsonSchemaKeyword.key())) {
                return true;
            }
        }
        return false;
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

    /**
     * Returns an Optional instance for a key in this object.  This method also takes care of registering any
     * errors with the appropriate path and/or context.
     *
     * @param property The name of the property you are retrieving
     * @param expected The type of JsonValue to be returned.
     * @param <X>      Method capture vararg to ensure type-safety for callers.
     * @return Optional.empty if the key doesn't exist, otherwise returns the value at the specified key.
     */
    private <X extends JsonValue> Optional<X> findByKey(String property, Class<X> expected) {
        checkNotNull(property, "property must not be null");
        if (wrapped.containsKey(property)) {
            JsonValue jsonValue = wrapped.get(property);
            if (!expected.isAssignableFrom(jsonValue.getClass())) {
                throw new UnexpectedValueException(path.child(property), jsonValue, expected);
            }
            return Optional.of(expected.cast(jsonValue));
        }
        return Optional.empty();
    }
}
