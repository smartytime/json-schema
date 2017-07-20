package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@AllArgsConstructor
public class PathAwareJsonValue {

    @NonNull
    private final JsonValue wrapped;

    @NonNull
    private final JsonPath path;

    public int arraySize() {
        return wrapped.asJsonArray().size();
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

    public JsonNumber asJsonNumber() {
        return (JsonNumber) wrapped;
    }

    /**
     * Return the JsonValue as a JsonObject
     *
     * @return the JsonValue as a JsonObject
     * @throws ClassCastException if the JsonValue is not a JsonObject
     * @since 1.1
     */
    public JsonObject asJsonObject() {
        return wrapped.asJsonObject();
    }

    @Nullable
    public String asString() {
        if(is(JsonValue.ValueType.NULL)) {
            return null;
        } else {
            return ((JsonString) wrapped).getString();
        }

    }

    public boolean containsKey(String key) {
        return wrapped.asJsonObject().containsKey(key);
    }

    public void forEach(BiConsumer<? super String, ? super PathAwareJsonValue> action) {
        wrapped.asJsonObject().forEach((k, v) -> {
            action.accept(k, new PathAwareJsonValue(v, path.child(k)));
        });
    }

    public void forEachIndex(BiConsumer<? super Integer, ? super PathAwareJsonValue> action) {
        AtomicInteger i = new AtomicInteger(0);
        wrapped.asJsonArray().forEach((v) -> {
            int idx = i.getAndIncrement();
            action.accept(idx, new PathAwareJsonValue(v, path.child(idx)));
        });
    }

    public PathAwareJsonValue get(String childKey) {
        checkNotNull(childKey, "childKey must not be null");
        return new PathAwareJsonValue(wrapped.asJsonObject().get(childKey), path.child(childKey));
    }

    public PathAwareJsonValue getItem(int idx) {
        JsonValue jsonValue = wrapped.asJsonArray().get(idx);
        return forChild(jsonValue, idx);
    }

    public JsonSchemaType getJsonSchemaType() {
        return JsonUtils.schemaTypeFor(wrapped);
    }

    public JsonValue.ValueType getValueType() {
        return wrapped.getValueType();
    }

    public boolean is(JsonValue.ValueType... types) {
        checkNotNull(types, "types must not be null");
        checkArgument(types.length > 0, "types must be >0");
        for (JsonValue.ValueType type : types) {
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

    public Stream<PathAwareJsonValue> streamArrayItems() {
        AtomicInteger i = new AtomicInteger(0);
        return wrapped.asJsonArray().stream()
                .map(jsonValue -> forChild(jsonValue, i.incrementAndGet()));
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

    public PathAwareJsonValue withValue(JsonValue value) {
        checkNotNull(value, "value must not be null");
        return new PathAwareJsonValue(value, path);
    }

    private PathAwareJsonValue forChild(JsonValue child, int idx) {
        return new PathAwareJsonValue(child, path.child(idx));
    }
}
