package org.everit.jsoniter.jsr353;

import com.jsoniter.any.Any;
import lombok.experimental.var;

import javax.json.JsonStructure;
import javax.json.JsonValue;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jsoniter.ValueType.BOOLEAN;
import static com.jsoniter.ValueType.INVALID;
import static com.jsoniter.ValueType.NUMBER;
import static com.jsoniter.ValueType.STRING;

public abstract class JsoniterStructure<K> extends JsoniterValue implements JsonStructure {

    /**
     * We use the same structure whether it's a map or list internally.  This just caches any of the jsr353 stuff.
     */
    private final ConcurrentHashMap<K, JsonValue> internal = new ConcurrentHashMap<>();
    private final List<JsonValue> internalList = new CopyOnWriteArrayList<>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    public JsoniterStructure(Any wrapped) {
        super(wrapped);
    }

    protected abstract Stream<K> cacheKeyStream();

    /**
     * This method is a little wonky because we want to prevent data that's masquerading
     * as a type it shouldn't be.  Jsoniter will do some type coersion that we want to
     * avoid for the sake of consistency.
     *
     * @param key        The key or index to look up inside this element.
     * @param validTypes Optional.  If passed, will validate that the referenced instance is one of these types
     * @return The referenced Any.  Will be empty if it's not found, null, or the wrong type.
     */
    protected Optional<Any> any(K key, com.jsoniter.ValueType... validTypes) {
        final Any any = wrapped.get(key);
        final com.jsoniter.ValueType anyType = any.valueType();
        if ((anyType == INVALID) || (anyType == com.jsoniter.ValueType.NULL)) {
            return Optional.empty();
        } else {
            if (validTypes != null && validTypes.length > 0) {
                for (com.jsoniter.ValueType validType : validTypes) {
                    if (anyType == validType) {
                        return Optional.of(any);
                    }
                }
                return Optional.empty();
            }
            return Optional.of(any);
        }
    }

    protected List<JsonValue> internalList() {
        internal();
        return internalList;
    }

    protected Map<K, JsonValue> internal() {
        if (!loaded.get()) {
            synchronized (loaded) {
                if (!loaded.get()) {
                    cacheKeyStream().forEach(k -> {
                        var value = this.getOrLoadByKey(k);
                        internalList.add(value);
                    });
                }
                loaded.set(true);
            }
        }
        return internal;
    }

    protected Optional<String> findString(K key) {
        return any(key, STRING).map(Any::toString);
    }

    protected Optional<Number> findNumber(K key) {
        return any(key, NUMBER).map(Any::toDouble);
    }

    protected Optional<Boolean> findBoolean(K key) {
        return any(key, BOOLEAN).map(Any::toBoolean);
    }

    static javax.json.JsonValue jsonValueOf(Any next) {
        checkNotNull(next, "next must not be null");

        switch (next.valueType()) {
            case ARRAY:
                return new JsoniterArray(next);
            case BOOLEAN:
                return next.toBoolean() ? javax.json.JsonValue.TRUE : javax.json.JsonValue.FALSE;
            case NULL:
                return javax.json.JsonValue.NULL;
            case NUMBER:
                return new JsoniterNumber(next);
            case OBJECT:
                return new JsoniterObject(next);
            case STRING:
                return new JsoniterString(next);
            default:
                throw new IllegalStateException("Unable to process type: " + next.valueType());
        }
    }

    /**
     * This method looks up the object by key, computes it if not already processed, and returns the
     * result.
     *
     * @param key      THe key or index
     * @param expected The class expected to be returned.  ClassCastException will be thrown if mismatched.
     * @return Optional JsonValue to be returned.
     */
    <X extends javax.json.JsonValue> Optional<X> find(K key, Class<X> expected) {
        final JsonValue jsonValue = getOrLoadByKey(key);
        try {
            return Optional.ofNullable((X) jsonValue);
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("Expected %s but was %s",
                    expected, jsonValue.getClass()));
        }
    }

    <X extends javax.json.JsonValue> X get(K key, Class<X> expected, Supplier<? extends RuntimeException> exception) {
        return find(key, expected).orElseThrow(exception);
    }

    /**
     * This method looks up the object by key, computes it if not already processed, and returns the
     * result.
     *
     * @param key The key or index
     * @return Optional JsonValue to be returned.
     */
    private JsonValue getOrLoadByKey(K key) throws InvalidKeyException {
        return internal.computeIfAbsent(key, (k) -> {
            final Any any = wrapped.get(key);
            final var valueType = any.valueType();
            if (valueType == INVALID) {
                throw new InvalidKeyException();
            }
            return jsonValueOf(any);
        });
    }
}
