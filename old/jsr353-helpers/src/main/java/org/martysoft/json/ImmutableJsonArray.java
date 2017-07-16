package io.dugnutt.json;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.Collection;

public interface ImmutableJsonArray extends JsonArray {

    @Override
    default boolean add(JsonValue jsonValue) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    default boolean addAll(Collection<? extends JsonValue> c) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    default boolean addAll(int index, Collection<? extends JsonValue> c) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    default ValueType getValueType() {
        return ValueType.ARRAY;
    }
}
