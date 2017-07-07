package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;

import java.util.List;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public interface JsonElement<X> {

    default JsonArray<?> asArray() {
        throw new UnexpectedValueException(javaType(), JsonArray.class);
    }

    default Boolean asBoolean() {
        throw new UnexpectedValueException(javaType(), Boolean.class);
    }

    default Integer asInteger() {
        throw new UnexpectedValueException(javaType(), Integer.class);
    }

    default Number asNumber() {
        throw new UnexpectedValueException(javaType(), Number.class);
    }

    default JsonObject<?> asObject() {
        throw new UnexpectedValueException(javaType(), JsonObject.class);
    }

    default String asString() {
        throw new UnexpectedValueException(javaType(), String.class);
    }

    Class<?> javaType();

    JsonSchemaType type();

    Object raw();
    X unbox();

    JsonApi<?> api();

    List<String> path();

    default void doOneOf(Consumer<MultiplexingLoader<X>> consumer) {
        checkNotNull(consumer, "consumer must not be null");
        MultiplexingLoader<X> loader = new MultiplexingLoader<X>(this);
        consumer.accept(loader);
        try {
            loader.execute();
        } catch (Exception e) {
            throw new MultiplexingFailure("Error during execution", e);
        }
    }


}

