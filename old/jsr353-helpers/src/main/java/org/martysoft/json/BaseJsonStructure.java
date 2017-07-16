package io.dugnutt.json;

import lombok.experimental.var;

import javax.json.JsonValue;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class BaseJsonStructure<T, K> {

    /**
     * We use the same structure whether it's a map or list internally.  This just caches any of the jsr353 stuff.
     */
    private final Map<K, JsonValue> internal;
    private final List<JsonValue> internalList;
    private final AtomicBoolean loaded;

    public BaseJsonStructure(Map<K, JsonValue> internal) {
        this.internal = internal;
        internalList = new CopyOnWriteArrayList<>();
        loaded = new AtomicBoolean(false);
    }

    public BaseJsonStructure() {
        internal = new ConcurrentHashMap<>();
        internalList = new CopyOnWriteArrayList<>();
        loaded = new AtomicBoolean(false);
    }

    public abstract JsonValue jsonValueOf(T next);

    /**
     * Returns a stream of keys that represents the keys in the underlying store you wish to cache.
     *
     * @return
     */
    protected abstract Stream<K> cacheKeyStream();

    public abstract JsonValue.ValueType jsonTypeOf(T t);

    public abstract Function<T, String> getString();

    public abstract Function<T, Number> getNumber();

    public abstract Function<T, Boolean> getBoolean();

    public boolean isNull(K index) {
        return jsonTypeOf(fetch(index)) == JsonValue.ValueType.NULL;
    }

    /**
     * This method is a little wonky because we want to prevent data that's masquerading
     * as a type it shouldn't be.  Jsoniter will do some type coersion that we want to
     * avoid for the sake of consistency.
     *
     * @param key        The key or index to look up inside this element.
     * @param validTypes Optional.  If passed, will validate that the referenced instance is one of these types
     * @return The referenced Any.  Will be empty if it's not found, null, or the wrong type.
     */
    protected Optional<T> any(K key, JsonValue.ValueType... validTypes) {
        final T t = fetch(key);
        final JsonValue.ValueType tType = jsonTypeOf(t);
        if ((tType == null) || (tType == JsonValue.ValueType.NULL)) {
            return Optional.empty();
        } else {
            if (validTypes != null && validTypes.length > 0) {
                for (JsonValue.ValueType validType : validTypes) {
                    if (tType == validType) {
                        return Optional.of(t);
                    }
                }
                return Optional.empty();
            }
            return Optional.of(t);
        }
    }

    protected List<JsonValue> internalList() {
        internalMap();
        return internalList;
    }

    protected Map<K, JsonValue> internalMap() {
        if (!loaded.get()) {
            synchronized (loaded) {
                if (!loaded.get()) {
                    cacheKeyStream().forEach(k -> {
                        try {
                            var value = this.getOrLoadByKey(k);
                            internalList.add(value);
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        }
                    });
                }
                loaded.set(true);
            }
        }
        return internal;
    }

    protected Optional<String> findString(K key) {
        return any(key, JsonValue.ValueType.STRING).map(getString());
    }

    protected Optional<Number> findNumber(K key) {
        return any(key, JsonValue.ValueType.NUMBER).map(this.getNumber());
    }

    protected Optional<Boolean> findBoolean(K key) {
        return any(key, JsonValue.ValueType.TRUE, JsonValue.ValueType.FALSE).map(this.getBoolean());
    }

    protected abstract T fetch(K k);

    /**
     * This method looks up the object by key, computes it if not already processed, and returns the
     * result.
     *
     * @param key      THe key or index
     * @param expected The class expected to be returned.  ClassCastException will be thrown if mismatched.
     * @return Optional JsonValue to be returned.
     */
    <X extends JsonValue> Optional<X> find(K key, Class<X> expected) {
        final JsonValue jsonValue;
        try {
            jsonValue = getOrLoadByKey(key);
        } catch (InvalidKeyException e) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable((X) jsonValue);
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("Expected %s but was %s",
                    expected, jsonValue.getClass()));
        }
    }

    <X extends JsonValue> X get(K key, Class<X> expected, Supplier<? extends RuntimeException> exception) {
        return find(key, expected).orElseThrow(exception);
    }

    /**
     * This method looks up the object by key, computes it if not already processed, and returns the
     * result.
     *
     * @param key The key or index
     * @return JsonValue to be returned.
     */
    private JsonValue getOrLoadByKey(K key) throws InvalidKeyException {
        return internal.computeIfAbsent(key, (k) -> {
            final T t = fetch(k);

            final var valueType = jsonTypeOf(t);
            if (valueType == null) {
                throw new InvalidKeyException();
            }
            return jsonValueOf(t);
        });
    }
}
