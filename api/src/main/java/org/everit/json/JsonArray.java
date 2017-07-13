package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author erosb
 */
public interface JsonArray<X> extends JsonValue<X>, Iterable<JsonElement<?>> {

    JsonElement<?> get(int i);

    int length();

    @Override
    default JsonArray<?> asArray() {
        return this;
    }

    List<JsonElement<?>> toList();

    @Override
    default JsonSchemaType schemaType() {
        return JsonSchemaType.Array;
    }

    @Override
    default Iterator<JsonElement<?>> iterator() {
        return toList().iterator();
    }

    @Override
    default void forEach(Consumer<? super JsonElement<?>> action) {
        toList().forEach(action);
    }

    @Override
    default Spliterator<JsonElement<?>> spliterator() {
        return toList().spliterator();
    }
}
