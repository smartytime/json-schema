package org.martysoft.json;

import com.google.common.base.Preconditions;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class JsonArrayImpl implements ImmutableJsonArray {

    private final List<JsonValue> internal;

    public JsonArrayImpl(List<JsonValue> internal) {
        Preconditions.checkNotNull(internal, "internal must not be null");
        this.internal = new CopyOnWriteArrayList<>(internal);
    }

    @Override
    public JsonObject getJsonObject(int index) {
        return getValueAs(index, JsonObject.class).orElse(null);
    }

    @Override
    public JsonArray getJsonArray(int index) {
        return getValueAs(index, JsonArray.class).orElse(null);
    }

    @Override
    public JsonNumber getJsonNumber(int index) {
        return getValueAs(index, JsonNumber.class).orElse(null);
    }

    @Override
    public JsonString getJsonString(int index) {
        return getValueAs(index, JsonString.class).orElse(null);
    }

    @Override
    public <T extends JsonValue> List<T> getValuesAs(Class<T> clazz) {
        return internal.stream().map(clazz::cast).collect(Collectors.toList());
    }

    private <X extends JsonValue> Optional<X> getValueAs(int idx, Class<X> expected) {
        X value = (X) internal.get(idx);
        if (value == NULL) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(value);
        }
    }

    @Override
    public String getString(int index) {
        return getValueAs(index, JsonString.class).map(JsonString::getString).orElse(null);
    }

    @Override
    public String getString(int index, String defaultValue) {
        return getValueAs(index, JsonString.class).map(JsonString::getString).orElse(defaultValue);
    }

    @Override
    public int getInt(int index) {
        return getValueAs(index, JsonNumber.class).map(JsonNumber::intValue).orElse(0);
    }

    @Override
    public int getInt(int index, int defaultValue) {
        return getValueAs(index, JsonNumber.class).map(JsonNumber::intValue).orElse(defaultValue);

    }

    @Override
    public boolean getBoolean(int index) {
        return getValueAs(index, JsonValue.class).map(v->{
            if(v == TRUE) {
                return true;
            } else {
                return false;
            }
        }).orElse(false);

    }

    @Override
    public boolean getBoolean(int index, boolean defaultValue) {
        return getValueAs(index, JsonValue.class).map(v->{
            if(v == TRUE) {
                return true;
            } else {
                return false;
            }
        }).orElse(defaultValue);
    }

    @Override
    public boolean isNull(int index) {
        return getValueAs(index, JsonValue.class).map(v -> v == NULL).orElse(false);
    }

    @Override
    public int size() {
        return internal.size();
    }

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return internal.contains(o);
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return internal.iterator();
    }

    @Override
    public Object[] toArray() {
        return internal.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return internal.toArray(a);
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        return internal.containsAll(c);
    }

    @Override
    public JsonValue get(int index) {
        return internal.get(index);
    }

    @Override
    public JsonValue set(int index, JsonValue element) {
        return internal.set(index, element);
    }

    @Override
    public void add(int index, JsonValue element) {
        internal.add(index, element);
    }

    @Override
    public JsonValue remove(int index) {
        return internal.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return internal.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return internal.lastIndexOf(o);
    }

    @Override
    public ListIterator<JsonValue> listIterator() {
        return internal.listIterator();
    }

    @Override
    public ListIterator<JsonValue> listIterator(int index) {
        return internal.listIterator(index);
    }

    @Override
    public List<JsonValue> subList(int fromIndex, int toIndex) {
        return internal.subList(fromIndex, toIndex);
    }
}
