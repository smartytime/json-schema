package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;

/**
 * This class is used for convenience in accessing data within a JsonObject.  It wraps the JSR353 {@link JsonObject}
 * and adds some extra methods that allow more fluent usage.
 */
@Getter
public class JsonValueWithLocation implements PartialJsonObject {

    @NonNull
    private final JsonValue wrapped;

    @NonNull
    private final SchemaLocation location;

    private JsonValueWithLocation(JsonValue wrapped, SchemaLocation location) {
        this.wrapped = checkNotNull(wrapped);
        this.location = checkNotNull(location);
    }

    public ValueType getValueType() {
        return wrapped.getValueType();
    }

    public JsonObject asJsonObject() {
        return wrapped.asJsonObject();
    }

    public JsonArray asJsonArray() {
        return wrapped.asJsonArray();
    }

    public JsonSchemaType getJsonSchemaType() {
        return JsonUtils.schemaTypeFor(wrapped);
    }

    public int arraySize() {
        return wrapped.asJsonArray().size();
    }

    public JsonNumber asJsonNumber() {
        return (JsonNumber) wrapped;
    }

    @Nullable
    public String asString() {
        if (is(ValueType.NULL)) {
            return null;
        } else {
            return ((JsonString) wrapped).getString();
        }
    }

    public JsonPath getPath() {
        return location.getJsonPath();
    }

    public boolean containsKey(String key) {
        return wrapped.asJsonObject().containsKey(key);
    }

    public JsonArray expectArray(JsonSchemaKeyword property) {
        return findArray(property).orElseThrow(() -> new MissingExpectedPropertyException(asJsonObject(), property.key()));
    }

    public Optional<JsonArray> findArray(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonArray.class);
    }

    public Optional<Boolean> findBoolean(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");

        JsonObject jsonObject = asJsonObject();
        if (jsonObject.containsKey(property.key()) && !jsonObject.isNull(property.key())) {
            try {
                return Optional.of(jsonObject.getBoolean(property.key()));
            } catch (ClassCastException e) {
                throw new UnexpectedValueException(location.getJsonPath(), jsonObject.get(property.key()), ValueType.TRUE, ValueType.FALSE);
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

    public Optional<JsonValueWithLocation> findPathAwareObject(JsonSchemaKeyword keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return findPathAwareObject(keyword.key());
    }

    public Optional<JsonValueWithLocation> findPathAwareObject(String childKey) {
        checkNotNull(childKey, "childKey must not be null");
        return findObject(childKey).map(json -> fromJsonValue(json, location.withChildPath(childKey)));
    }

    public JsonValueWithLocation getPathAwareObject(JsonSchemaKeyword keyword) {
        return getPathAwareObject(keyword.key());
    }

    public Optional<String> findString(JsonSchemaKeyword property) {
        checkNotNull(property, "property must not be null");
        return findByKey(property.key(), JsonString.class)
                .map(JsonString::getString);
    }

    public void forEachIndex(BiConsumer<? super Integer, ? super JsonValueWithLocation> action) {
        AtomicInteger i = new AtomicInteger(0);
        wrapped.asJsonArray().forEach((v) -> {
            int idx = i.getAndIncrement();
            action.accept(idx, new JsonValueWithLocation(v, location.withChildPath(idx)));
        });
    }

    public void forEachKey(BiConsumer<? super String, ? super JsonValueWithLocation> action) {
        wrapped.asJsonObject().forEach((k, v) -> {
            action.accept(k, fromJsonValue(v, location.withChildPath(k)));
        });
    }

    @Override
    @Deprecated
    public JsonValue get(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalStateException("Only string keys are allowed");
        }
        return asJsonObject().get(key);
    }

    public JsonValueWithLocation getItem(int idx) {
        JsonValue jsonValue = wrapped.asJsonArray().get(idx);
        return fromJsonValue(jsonValue, location.withChildPath(idx));
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

    public JsonValueWithLocation getPathAwareObject(String childKey) {
        checkNotNull(childKey, "childKey must not be null");
        return fromJsonValue(wrapped.asJsonObject().get(childKey), location.withChildPath(childKey));
    }

    public String getString(JsonSchemaKeyword property) {
        try {
            if (asJsonObject().isNull(property.key())) {
                return null;
            }
            return asJsonObject().getString(property.key());
        } catch (ClassCastException e) {
            throw new UnexpectedValueException(location, asJsonObject().get(property.key()), ValueType.STRING);
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
        return location.getJsonPointerFragment() + " -> " + wrapped;
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

    public boolean isEmpty() {
        return asJsonObject().isEmpty();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return asJsonObject().entrySet();
    }

    public Stream<JsonValueWithLocation> streamPathAwareArrayItems(JsonSchemaKeyword keyword) {
        AtomicInteger i = new AtomicInteger(0);
        if (!has(keyword, ValueType.ARRAY)) {
            return Stream.empty();
        } else {
            return expectArray(keyword)
                    .stream()
                    .map(jsonValue -> fromJsonValue(jsonValue, location.withChildPath(i.incrementAndGet())));
        }
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
                throw new UnexpectedValueException(location.withChildPath(property), jsonValue, valueType);
            }
            return Optional.of(expected.cast(jsonValue));
        }
        return Optional.empty();
    }

    public static JsonValueWithLocation fromJsonValue(JsonValue jsonObject, SchemaLocation parentLocation) {
        final URI uri;
        if (jsonObject.getValueType() == ValueType.OBJECT) {
            uri = JsonUtils.extract$IdFromObject(jsonObject.asJsonObject());
        } else {
            uri = null;
        }
        if (uri != null) {
            return new JsonValueWithLocation(jsonObject, parentLocation.withId(uri));
        } else {
            return new JsonValueWithLocation(jsonObject, parentLocation);
        }
    }

    public static JsonValueWithLocation fromJsonValue(JsonObject jsonObject) {
        final SchemaLocation rootSchemaLocation;
        if (jsonObject.containsKey($ID.key())) {
            String $id = jsonObject.getString($ID.key());
            rootSchemaLocation = SchemaLocation.schemaLocation($id);
        } else {
            rootSchemaLocation = SchemaLocation.anonymousRoot();
        }

        return  new JsonValueWithLocation(jsonObject, rootSchemaLocation);
    }
}
