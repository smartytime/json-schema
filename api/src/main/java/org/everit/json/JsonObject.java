package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaProperty;
import org.everit.jsonschema.api.JsonSchemaType;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author erosb
 */
public interface JsonObject<X> extends JsonValue<X> {
    default boolean isEmpty() {
        return properties().isEmpty();
    }

    // protected JsonObject(Map<String, ?> storage) {
    //     super(storage);
    //     this.storage = storage;
    //     this.ls = new LoadingState(SchemaLoader.builder()
    //             .rootSchemaJson(this)
    //             .schemaJson(this)).childForId(storage.get("id"));
    // }
    //
    // protected JsonObject(Map<String, Object> storage, LoadingState ls) {
    //     super(storage, ls.childForId(requireNonNull(storage, "storage cannot be null").get("id")));
    //     this.storage = storage;
    // }

    Set<String> properties();

    default boolean hasKey(String key) {
        return properties().contains(key);
    }

    default boolean has(JsonSchemaProperty key) {
        return properties().contains(key.getKey());
    }

    default int numberOfProperties() {
        return properties().size();
    }

    default void forEach(JsonObjectIterator iterator) {
        properties().stream()
                .forEach(propName->{
                    JsonElement<?> element = git(propName);
                    iterator.apply(propName, element);
                });
    }

    @Override
    default JsonObject<?> asObject() {
        return this;
    }

    default JsonElement<?> get(JsonSchemaProperty property) {
        return git(property.getKey());
    }

    default JsonElement<?> git(String key) {
        return find(key).orElseThrow(()->new MissingExpectedPropertyException(this, key));
    }

    default Optional<JsonElement<?>> find(JsonSchemaProperty key) {
        checkNotNull(key, "key must not be null");
        return find(key.getKey());
    }

    Optional<JsonElement<?>> find(String key);

    // boolean containsKey(String key) {
    //     return storage.containsKey(key);
    // }

    // Optional<JsonValue> maybe(String key) {
    //     return maybeMapping(key, identity());
    // }
    //
    // <R> Optional<R> maybeMapping(String key, Function<JsonValue, R> fn) {
    //     if (storage.containsKey(key)) {
    //         return Optional.of(fn.apply(childFor(key)));
    //     } else {
    //         return Optional.empty();
    //     }
    // }
    //
    // void forEach(JsonObjectIterator iterator) {
    //     storage.entrySet().forEach(entry -> iterateOnEntry(entry, iterator));
    // }
    //
    // private void iterateOnEntry(Map.Entry<String, Object> entry, JsonObjectIterator iterator) {
    //     String key = entry.getKey();
    //     iterator.apply(key, childFor(key));
    // }
    //
    // @Override public <R> R requireObject(Function<JsonObject, R> mapper) {
    //     return mapper.apply(this);
    // }

    default JsonSchemaType schemaType() {
        return JsonSchemaType.Object;
    }
}
