package org.everit.json.schema.loader;

import org.everit.json.JsonApi;
import org.everit.json.JsonElement;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.loader.internal.ReferenceResolver;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
final class JsonObject extends JsonValue implements org.everit.json.JsonObject<JsonObject> {

    private final Map<String, Object> storage;
    private final LegacyJsonObjectApi legacyApi = new LegacyJsonObjectApi();

    JsonObject(Map<String, Object> storage) {
        super(storage);
        this.storage = storage;
        this.ls = new LoadingState(SchemaLoader.builder()
                .rootSchemaJson(this)
                .schemaJson(this)).childForId(storage.get("id"));
    }

    JsonObject(Map<String, Object> storage, LoadingState ls) {
        super(storage, ls.childForId(requireNonNull(storage, "storage cannot be null").get("id")));
        this.storage = storage;
    }

    JsonValue childFor(String key) {
        LoadingState childState = ls.childFor(key);
        return JsonValue.of(storage.get(key), childState);
    }

    boolean containsKey(String key) {
        return storage.containsKey(key);
    }

    void require(String key, Consumer<JsonValue> consumer) {
        if (storage.containsKey(key)) {
            consumer.accept(childFor(key));
        } else {
            throw failureOfMissingKey(key);
        }
    }

    JsonValue require(String key) {
        return requireMapping(key, e -> e);
    }

    <R> R requireMapping(String key, Function<JsonValue, R> fn) {
        if (storage.containsKey(key)) {
            return fn.apply(childFor(key));
        } else {
            throw failureOfMissingKey(key);
        }
    }

    private SchemaException failureOfMissingKey(String key) {
        return ls.createSchemaException(format("required key [%s] not found", key));
    }

    void maybe(String key, Consumer<JsonValue> consumer) {
        if (storage.containsKey(key)) {
            consumer.accept(childFor(key));
        }
    }

    Optional<JsonValue> maybe(String key) {
        return maybeMapping(key, identity());
    }

    <R> Optional<R> maybeMapping(String key, Function<JsonValue, R> fn) {
        if (storage.containsKey(key)) {
            return Optional.of(fn.apply(childFor(key)));
        } else {
            return Optional.empty();
        }
    }

    void forEach(JsonObjectIterator iterator) {
        storage.entrySet().forEach(entry -> iterateOnEntry(entry, iterator));
    }

    private void iterateOnEntry(Map.Entry<String, Object> entry, JsonObjectIterator iterator) {
        String key = entry.getKey();
        iterator.apply(key, childFor(key));
    }

    @Override public <R> R requireObject(Function<JsonObject, R> mapper) {
        return mapper.apply(this);
    }

    @Override protected Class<?> typeOfValue() {
        return JsonObject.class;
    }

    @Override protected Object value() {
        return this;
    }

    Map<String, Object> toMap() {
        return unmodifiableMap(storage);
    }

    // boolean isEmpty() {
    //     return storage.isEmpty();
    // }

    public Set<String> keySet() {
        return unmodifiableSet(storage.keySet());
    }

    public Object get(String name) {
        return storage.get(name);
    }

    @Override
    public Set<String> properties() {
        return keySet();
    }

    @Override
    public JsonApi<?> api() {
        return new LegacyJsonObjectApi();
    }

    @Override
    public JsonObject unbox() {
        return this;
    }

    @Override
    public Optional<JsonElement<?>> find(String key) {
        return this.maybe(key).map(legacyApi::of);
    }
}
