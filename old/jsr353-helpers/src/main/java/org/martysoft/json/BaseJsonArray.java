package io.dugnutt.json;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class BaseJsonArray<T> extends BaseJsonStructure<T, Integer> implements JsonArray {

    @Override
    public JsonObject getJsonObject(int index) {
        return get(index, JsonObject.class, exceptionSupplier(index));
    }

    @Override
    public JsonArray getJsonArray(int index) {
        return get(index, JsonArray.class, exceptionSupplier(index));
    }

    @Override
    public JsonNumber getJsonNumber(int index) {
        return get(index, JsonNumber.class, exceptionSupplier(index));
    }

    @Override
    public JsonString getJsonString(int index) {
        return get(index, JsonString.class, exceptionSupplier(index));
    }

    @Override
    public <T extends JsonValue> List<T> getValuesAs(Class<T> clazz) {
        return internalList().stream()
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    @Override
    public String getString(int index) {
        return get(index, JsonString.class, exceptionSupplier(index)).getString();
    }

    @Override
    public String getString(int index, String defaultValue) {
        return findString(index).orElse(defaultValue);
    }

    @Override
    public int getInt(int index) {
        return findNumber(index).map(Number::intValue)
                .orElseThrow(exceptionSupplier(index));
    }

    @Override
    public int getInt(int index, int defaultValue) {
        return findNumber(index)
                .map(Number::intValue)
                .orElse(defaultValue);
    }

    @Override
    public boolean getBoolean(int index) {
        return findBoolean(index).orElseThrow(exceptionSupplier(index));
    }

    @Override
    public boolean getBoolean(int index, boolean defaultValue) {
        return findBoolean(index).orElse(defaultValue);
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Returns the value type of this JSON value.
     *
     * @return JSON value type
     */
    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    @Override
    public boolean contains(Object o) {
        //todo:ericm
        return internalList().contains(o);
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return internalList().iterator();
    }

    @Override
    public Object[] toArray() {
        return internalList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return internalList().toArray(a);
    }



    @Override
    public boolean containsAll(Collection<?> c) {
        return internalList().containsAll(c);
    }


    @Override
    public JsonValue get(int index) {
        return any(index)
                .map(this::jsonValueOf)
                .filter(Objects::nonNull)
                .orElseThrow(exceptionSupplier(index));
    }

    @Override
    public JsonValue set(int index, JsonValue element) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    public void add(int index, JsonValue element) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    public JsonValue remove(int index) {
        throw new UnsupportedOperationException("JSON structure is not mutable");
    }

    @Override
    public int indexOf(Object o) {
        return internalList().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return internalList().lastIndexOf(o);
    }

    @Override
    public ListIterator<JsonValue> listIterator() {
        return internalList().listIterator();
    }

    @Override
    public ListIterator<JsonValue> listIterator(int index) {
        return internalList().listIterator(index);
    }

    @Override
    public List<JsonValue> subList(int fromIndex, int toIndex) {
        return internalList().subList(fromIndex, toIndex);
    }

    /**
     * Returns a stream of keys that represents the keys in the underlying store you wish to cache.
     *
     * @return
     */
    @Override
    protected Stream<Integer> cacheKeyStream() {
        //A little weird, but it tells which items need to be loaded.
        return IntStream.range(0, size()).boxed();
    }

    private static Supplier<IndexOutOfBoundsException> exceptionSupplier(int index) {
        return () -> new IndexOutOfBoundsException("Unable to find item at index=" + index);
    }
}
