package org.martysoft.json;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Map;

public interface ImmutableJsonObject extends JsonObject {

    default JsonValue put(String key, JsonValue value) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    default JsonValue remove(Object key) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    default void putAll(Map<? extends String, ? extends JsonValue> m) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    default void clear() {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    default ValueType getValueType() {
        return ValueType.OBJECT;
    }
}
