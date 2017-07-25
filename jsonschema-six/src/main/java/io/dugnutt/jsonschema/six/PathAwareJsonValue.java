package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class is used for convenience in accessing data within a JsonObject.  It wraps the JSR353 {@link JsonObject}
 * and adds some extra methods that allow more fluent usage.
 */
@Getter
@AllArgsConstructor
public class PathAwareJsonValue implements PartialJsonObject {

    @NonNull
    private final JsonValue wrapped;

    @NonNull
    private final JsonPath path;

    public int arraySize() {
        return wrapped.asJsonArray().size();
    }

    public JsonNumber asJsonNumber() {
        return (JsonNumber) wrapped;
    }

    public JsonObject asJsonObject() {
        return wrapped.asJsonObject();
    }

    /**
     * Return the JsonValue as a JsonArray
     *
     * @return the JsonValue as a JsonArray
     * @throws ClassCastException if the JsonValue is not a JsonArray
     * @since 1.1
     */
    public JsonArray asJsonArray() {
        return wrapped.asJsonArray();
    }

    @Nullable
    public String asString() {
        if (is(ValueType.NULL)) {
            return null;
        } else {
            return ((JsonString) wrapped).getString();
        }
    }

    public boolean containsKey(String key) {
        return wrapped.asJsonObject().containsKey(key);
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return asJsonObject().entrySet();
    }

    public JsonArray expectArray(JsonSchemaKeyword property) {
        return findArray(property).orElseThrow(() -> new MissingExpectedPropertyException(asJsonObject(), property.key()));
    }

    public Number expectNumber(JsonSchemaKeyword property) {
        return findNumber(property).orElseThrow(() -> new MissingExpectedPropertyException(asJsonObject(), property.key()));
    }

    public JsonObject expectObject(JsonSchemaKeyword property) {
        return findObject(property).orElseThrow(() -> new MissingExpectedPropertyException(asJsonObject(), property.key()));
    }

    public String expectString(JsonSchemaKeyword property) {
        return findString(property).orElseThrow(() -> new MissingExpectedPropertyException(asJsonObject(), property.key()));
    }

    public Optional<JsonArray> findArray(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonArray.class);
    }

    // public JsonValue.ValueType getValueType() {
    //     return wrapped.getValueType();
    // }

    public Optional<Boolean> findBoolean(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        JsonObject jsonObject = asJsonObject();
        if (jsonObject.containsKey(property.key()) && !jsonObject.isNull(property.key())) {
            try {
                return Optional.of(jsonObject.getBoolean(property.key()));
            } catch (ClassCastException e) {
                throw new UnexpectedValueException(path, jsonObject.get(property.key()), ValueType.TRUE, ValueType.FALSE);
            }
        }
        return Optional.empty();
    }

    public Optional<JsonValue> findByKey(JsonSchemaKeyword prop) {
        JsonObject jsonObject = asJsonObject();
        if (jsonObject.containsKey(prop.key())) {
            return Optional.of(jsonObject.get(prop.key()));
        }
        return Optional.empty();
    }

    public Optional<JsonObject> findIfObject(String property) {
        checkNotNull(property, "property must not be null");
        return findByKey(property, JsonStructure.class)
                .filter(childJson -> JsonObject.class.isAssignableFrom(childJson.getClass()))
                .map(JsonObject.class::cast);
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

    public Optional<JsonObject> findObject(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");
        return findObject(property.key());
    }

    public Optional<String> findString(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");
        return findByKey(property.key(), JsonString.class)
                .map(JsonString::getString);
    }

    public void forEachIndex(BiConsumer<? super Integer, ? super PathAwareJsonValue> action) {
        AtomicInteger i = new AtomicInteger(0);
        wrapped.asJsonArray().forEach((v) -> {
            int idx = i.getAndIncrement();
            action.accept(idx, new PathAwareJsonValue(v, path.child(idx)));
        });
    }

    public void forEachKey(BiConsumer<? super String, ? super PathAwareJsonValue> action) {
        wrapped.asJsonObject().forEach((k, v) -> {
            action.accept(k, new PathAwareJsonValue(v, path.child(k)));
        });
    }

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return asJsonObject().size();
    }

    public JsonValue get(String childKey) {
        if (!(childKey instanceof String)) {
            throw new IllegalStateException("Only string keys are allowed");
        }
        return wrapped.asJsonObject().get(childKey);
    }

    public PathAwareJsonValue getItem(int idx) {
        JsonValue jsonValue = wrapped.asJsonArray().get(idx);
        return forChild(jsonValue, idx);
    }

    @Override
    public JsonArray getJsonArray(String name) {
        return asJsonObject().getJsonArray(name);
    }

    @Override
    public JsonObject getJsonObject(String name) {
        return asJsonObject().getJsonObject(name);
    }

    @Override
    public JsonNumber getJsonNumber(String name) {
        return asJsonObject().getJsonNumber(name);
    }

    @Override
    public JsonString getJsonString(String name) {
        return asJsonObject().getJsonString(name);
    }

    public PathAwareJsonValue getPathAwareJsonArray(String key) {
        JsonArray jsonArray = getJsonArray(key);
        return this.forChild(jsonArray, key);
    }

    @Override
    public String getString(String name) {
        return asJsonObject().getString(name);
    }

    @Override
    public String getString(String name, String defaultValue) {
        return asJsonObject().getString(name, defaultValue);
    }

    @Override
    public int getInt(String name) {
        return asJsonObject().getInt(name);
    }

    @Override
    public int getInt(String name, int defaultValue) {
        return asJsonObject().getInt(name, defaultValue);
    }

    @Override
    public boolean getBoolean(String name) {
        return asJsonObject().getBoolean(name);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        return asJsonObject().getBoolean(name, defaultValue);
    }

    @Override
    public boolean isNull(String name) {
        return asJsonObject().isNull(name);
    }

    public ValueType getValueType() {
        return wrapped.getValueType();
    }

    public boolean isEmpty() {
        return asJsonObject().isEmpty();
    }

    @Override
    @Deprecated
    public JsonValue get(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalStateException("Only string keys are allowed");
        }
        return asJsonObject().get(key);
    }

    public JsonObject getJsonObject(JsonSchemaKeyword name) {
        try {
            return asJsonObject().getJsonObject(name.key());
        } catch (ClassCastException e) {
            throw new UnexpectedValueException(path, asJsonObject().get(name.key()), ValueType.OBJECT);
        }
    }

    public JsonSchemaType getJsonSchemaType() {
        return JsonUtils.schemaTypeFor(wrapped);
    }

    public PathAwareJsonValue getPathAware(String childKey) {
        checkNotNull(childKey, "childKey must not be null");
        return new PathAwareJsonValue(wrapped.asJsonObject().get(childKey), path.child(childKey));
    }

    public Optional<PathAwareJsonValue> findPathAware(JsonSchemaKeyword keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return findPathAware(keyword.key());
    }

    public Optional<PathAwareJsonValue> findPathAware(String childKey) {
        checkNotNull(childKey, "childKey must not be null");
        return findObject(childKey).map(json -> new PathAwareJsonValue(json, path.child(childKey)));
    }

    public String getString(JsonSchemaKeyword property) {
        try {
            if (asJsonObject().isNull(property.key())) {
                return null;
            }
            return asJsonObject().getString(property.key());
        } catch (ClassCastException e) {
            throw new UnexpectedValueException(path, asJsonObject().get(property.key()), ValueType.STRING);
        }
    }

    @Override
    public JsonValue getValue(String jsonPointer) {
        return asJsonObject().getValue(jsonPointer);
    }

    public boolean has(JsonSchemaKeyword property, ValueType... ofType) {
        final JsonValue jsonValue = asJsonObject().get(property.key());
        if (jsonValue == null) {
            return false;
        }
        if (ofType != null && ofType.length > 0) {
            for (ValueType valueType : ofType) {
                if (jsonValue.getValueType() == valueType) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean hasAny(JsonSchemaKeyword... property) {
        for (JsonSchemaKeyword jsonSchemaKeyword : property) {
            if (asJsonObject().containsKey(jsonSchemaKeyword.key())) {
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

    /**
     * Returns JSON text for this JSON value.
     *
     * @return JSON text
     */
    @Override
    public String toString() {
        return wrapped.toString();
    }

    public boolean is(ValueType... types) {
        checkNotNull(types, "keywords must not be null");
        checkArgument(types.length > 0, "keywords must be >0");
        for (ValueType type : types) {
            if (wrapped.getValueType() == type) {
                return true;
            }
        }
        return false;
    }

    public int numberOfProperties() {
        return wrapped.asJsonObject().keySet().size();
    }

    public Set<String> propertyNames() {
        return wrapped.asJsonObject().keySet();
    }

    public Stream<PathAwareJsonValue> getPathAwareArrayItems() {
        AtomicInteger i = new AtomicInteger(0);
        return wrapped.asJsonArray().stream()
                .map(jsonValue -> forChild(jsonValue, i.incrementAndGet()));
    }

    public Stream<PathAwareJsonValue> streamPathAwareArrayItems(JsonSchemaKeyword keyword) {
        AtomicInteger i = new AtomicInteger(0);
        if (!has(keyword, ValueType.ARRAY)) {
            return Stream.empty();
        } else {
            return expectArray(keyword)
                    .stream()
                    .map(jsonValue -> forChild(jsonValue, i.incrementAndGet()));
        }
    }

    public Stream<PathAwareJsonValue> getPathAwareArrayItems(ValueType valueType) {
        return getPathAwareArrayItems()
                .peek(item -> {
                    if (!item.is(valueType)) {
                        throw new UnexpectedValueException(item.getPath(), item.getWrapped(), valueType);
                    }
                });
    }

    public PathAwareJsonValue withValue(JsonValue value) {
        checkNotNull(value, "value must not be null");
        return new PathAwareJsonValue(value, path);
    }

    private PathAwareJsonValue forChild(JsonValue child, int idx) {
        return new PathAwareJsonValue(child, path.child(idx));
    }

    private PathAwareJsonValue forChild(JsonValue child, String key) {
        return new PathAwareJsonValue(child, path.child(key));
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
        if (asJsonObject().containsKey(property)) {
            JsonValue jsonValue = asJsonObject().get(property);
            if (!expected.isAssignableFrom(jsonValue.getClass())) {
                final ValueType valueType = JsonUtils.jsonTypeForClass(expected);
                throw new UnexpectedValueException(path.child(property), jsonValue, valueType);
            }
            return Optional.of(expected.cast(jsonValue));
        }
        return Optional.empty();
    }
}
