package io.dugnutt.jsonschema.six;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public interface PartialJsonObject extends JsonObject {

    @Override
    @Deprecated
    default boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonValue get(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonValue put(String key, JsonValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default JsonValue remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void putAll(Map<? extends String, ? extends JsonValue> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default Collection<JsonValue> values() {
        throw new UnsupportedOperationException();
    }

}
