package io.sbsp.jsonschema;

import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.utils.JsonUtils;
import io.sbsp.jsonschema.utils.SchemaPaths;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class is used for convenience in accessing data within a JsonObject.  It wraps the JSR353 {@link JsonObject}
 * and adds some extra methods that allow more fluent usage.
 */
@EqualsAndHashCode(of = {"wrapped", "location"})
public class JsonValueWithPath implements ReadOnlyJsonObject {

    @NonNull
    @Getter
    private final JsonValue wrapped;

    private final JsonValue root;

    private JsonObject jsonObject;

    @NonNull
    @Getter
    private final SchemaLocation location;

    private JsonValueWithPath(JsonValue root, JsonValue wrapped, SchemaLocation location) {
        this.root = checkNotNull(root, "root must not be null");
        this.wrapped = checkNotNull(wrapped, "wrapped must not be null");
        this.location = checkNotNull(location, "location must not be null");
    }

    public JsonObject getRoot() {
        return root.asJsonObject();
    }

    public ValueType getValueType() {
        return wrapped.getValueType();
    }

    public JsonObject asJsonObject() {
        if (jsonObject == null) {
            verifyType(ValueType.OBJECT);
            jsonObject = wrapped.asJsonObject();
        }
        return jsonObject;
    }

    public JsonArray asJsonArray() {
        return wrapped.asJsonArray();
    }

    public JsonSchemaType getJsonSchemaType() {
        return JsonUtils.schemaTypeFor(wrapped);
    }

    public boolean has(KeywordInfo<?> keywordType) {
        return asJsonObject().containsKey(keywordType.key());
    }

    public int arraySize() {
        verifyType(ValueType.ARRAY);
        return wrapped.asJsonArray().size();
    }

    public JsonNumber asJsonNumber() {
        verifyType(ValueType.NUMBER);
        return (JsonNumber) wrapped;
    }

    @Nullable
    public String asString() {
        if (is(ValueType.NULL)) {
            return null;
        } else {
            verifyType(ValueType.STRING);
            return ((JsonString) wrapped).getString();
        }
    }

    public boolean isBoolean() {
        return is(ValueType.TRUE, ValueType.FALSE);
    }

    public boolean isNull() {
        return wrapped == JsonValue.NULL;
    }

    public boolean isNotNull() {
        return wrapped != JsonValue.NULL;
    }

    private void verifyType(ValueType... expected) {
        for (ValueType valueType : expected) {
            if (getValueType() == valueType) {
                return;
            }
        }
        throw new UnexpectedValueException(location, wrapped, expected);
    }

    public JsonPath getPath() {
        return location.getJsonPath();
    }

    public boolean containsKey(String key) {
        return asJsonObject().containsKey(key);
    }

    public Optional<JsonValue> findByKey(KeywordInfo<?> prop) {
        if (asJsonObject().containsKey(prop.key())) {
            return Optional.of(asJsonObject().get(prop.key()));
        }
        return Optional.empty();
    }

    public Optional<Integer> findInt(KeywordInfo<?> property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonNumber.class)
                .map(JsonNumber::intValue);
    }

    public Optional<Integer> findInteger(KeywordInfo<?> property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonNumber.class)
                .map(JsonNumber::intValue);
    }

    public Optional<Number> findNumber(KeywordInfo<?> property) {
        checkNotNull(property, "property must not be null");

        return findByKey(property.key(), JsonNumber.class)
                .map(JsonNumber::bigDecimalValue);
    }

    public Optional<JsonObject> findObject(String property) {
        checkNotNull(property, "property must not be null");
        return findByKey(property, JsonObject.class);
    }

    public Optional<JsonValueWithPath> findPathAwareObject(KeywordInfo<?> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return findPathAwareObject(keyword.key());
    }

    public Optional<JsonValueWithPath> findPathAwareObject(String childKey) {
        checkNotNull(childKey, "childKey must not be null");
        return findObject(childKey).map(json -> fromJsonValue(root, json, location.child(childKey)));
    }

    public JsonValueWithPath path(KeywordInfo<?> keyword) {
        return path(keyword.key());
    }

    public Optional<String> findString(KeywordInfo<?> property) {
        checkNotNull(property, "property must not be null");
        return findByKey(property.key(), JsonString.class)
                .map(JsonString::getString);
    }

    public void forEachIndex(BiConsumer<? super Integer, ? super JsonValueWithPath> action) {
        AtomicInteger i = new AtomicInteger(0);
        wrapped.asJsonArray().forEach((v) -> {
            int idx = i.getAndIncrement();
            action.accept(idx, new JsonValueWithPath(root, v, location.child(idx)));
        });
    }

    public void forEachKey(BiConsumer<? super String, ? super JsonValueWithPath> action) {
        wrapped.asJsonObject().forEach((k, v) -> {
            action.accept(k, fromJsonValue(root, v, location.child(k)));
        });
    }

    @Override
    @Deprecated
    public JsonValue get(Object key) {
        return asJsonObject().get(key);
    }

    public JsonValueWithPath getItem(int idx) {
        JsonValue jsonValue = wrapped.asJsonArray().get(idx);
        return new JsonValueWithPath(root, jsonValue, location.child(idx));
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

    public JsonValueWithPath path(String childKey) {
        checkNotNull(childKey, "childKey must not be null");
        return fromJsonValue(root, asJsonObject().getOrDefault(childKey, NULL), location.child(childKey));
    }

    public String getString(KeywordInfo<?> property) {
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

    public boolean has(KeywordInfo<?> property, ValueType... ofType) {
        final Map<String, JsonValue> jsonObject = asJsonObject();
        if (!jsonObject.containsKey(property.key())) {
            return false;
        }
        if (ofType != null && ofType.length > 0) {
            final JsonValue jsonValue = jsonObject.get(property.key());
            for (ValueType valueType : ofType) {
                if (jsonValue.getValueType() == valueType) {
                    return true;
                }
            }
            return false;
        }
        return true;
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
        return asJsonObject().keySet().size();
    }

    public Set<String> propertyNames() {
        return asJsonObject().keySet();
    }

    @Override
    public int size() {
        return asJsonObject().size();
    }

    public boolean isEmpty() {
        return asJsonObject().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return asJsonObject().containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return asJsonObject().keySet();
    }

    @Override
    public Collection<JsonValue> values() {
        return asJsonObject().values();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return asJsonObject().entrySet();
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
                throw new UnexpectedValueException(location.child(property), jsonValue, valueType);
            }
            return Optional.of(expected.cast(jsonValue));
        }
        return Optional.empty();
    }

    public static JsonValueWithPath fromJsonValue(JsonValue root, JsonValue jsonObject, SchemaLocation location) {
        if (jsonObject.getValueType() == ValueType.OBJECT) {
            final JsonObject asJsonObject = jsonObject.asJsonObject();
            location = JsonUtils.extract$IdFromObject(asJsonObject, "$id", "id")
                    .map(location::withId)
                    .orElse(location);
        }
        return new JsonValueWithPath(root, jsonObject, location);
    }

    public static JsonValueWithPath fromJsonValue(JsonValue jsonObject) {
        checkNotNull(jsonObject, "jsonObject must not be null");

        if (jsonObject.getValueType() == ValueType.OBJECT) {
            final JsonObject asObject = jsonObject.asJsonObject();
            final SchemaLocation schemaLocation = SchemaPaths.fromDocument(asObject, "$id", "id");
            return new JsonValueWithPath(jsonObject.asJsonObject(), jsonObject, schemaLocation);
        }

        return new JsonValueWithPath(jsonObject, jsonObject, SchemaPaths.fromNonSchemaSource(jsonObject));
    }
}
