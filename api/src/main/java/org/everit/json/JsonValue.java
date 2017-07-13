package org.everit.json;

/**
 * @author erosb
 */
public interface JsonValue<X> extends JsonElement<X> {



    // class Multiplexer<R> {
    //
    //     protected Map<Class<?>, Function<?, R>> actions = new HashMap<>();
    //
    //     Multiplexer(Class<?> expectedType, Function<?, R> mapper) {
    //         actions.put(expectedType, mapper);
    //     }
    //
    //     <T> Multiplexer<R> orMappedTo(Class<T> expectedType, Function<T, R> mapper) {
    //         actions.put(expectedType, mapper);
    //         return this;
    //     }
    //
    //     R requireAny() {
    //         if (typeOfValue() == null) {
    //             throw multiplexFailure();
    //         }
    //         Function<Object, R> consumer = (Function<Object, R>) actions.keySet().stream()
    //                 .filter(clazz -> clazz.isAssignableFrom(typeOfValue()))
    //                 .findFirst()
    //                 .map(actions::get)
    //                 .orElseThrow(() -> multiplexFailure());
    //         return consumer.apply(value());
    //     }
    //
    //     private SchemaException multiplexFailure() {
    //         return ls.createSchemaException(typeOfValue(), actions.keySet());
    //     }
    //
    // }
    //
    // class VoidMultiplexer extends Multiplexer<Void> {
    //
    //     VoidMultiplexer(Class<?> expectedType, Consumer<?> consumer) {
    //         super(expectedType, obj -> {
    //             ((Consumer<Object>) consumer).accept(obj);
    //             return null;
    //         });
    //     }
    //
    //     <T> VoidMultiplexer or(Class<T> expectedType, Consumer<T> consumer) {
    //         actions.put(expectedType,  obj -> {
    //             ((Consumer<Object>) consumer).accept(obj);
    //             return null;
    //         });
    //         return this;
    //     }
    //
    // }

    // private static final Function<?, ?> IDENTITY = e -> e;
    //
    // static final <T, R> Function<T,  R> identity() {
    //     return (Function<T, R>) IDENTITY;
    // }
    //
    //
    // protected Object unwrap() {
    //     return value();
    // }


    // protected LoadingState ls;

    // only called from JsonObject
    // protected JsonValue(Object obj) {
    //     this.obj = obj;
    // }

    // public <T> VoidMultiplexer canBe(Class<T> expectedType, Consumer<T> consumer) {
    //     return new VoidMultiplexer(expectedType, consumer);
    // }
    //
    // public <T, R> Multiplexer<R> canBeMappedTo(Class<T> expectedType, Function<T, R> mapper) {
    //     return new Multiplexer<R>(expectedType, mapper);
    // }

    // Object value();

    default boolean isEquals(Object o) {
    if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JsonValue that = (JsonValue) o;

        return raw() != null ? raw().equals(that.raw()) : that.raw() == null;
    }


    default int getHashCode() {
        return raw() != null ? raw().hashCode() : 0;
    }

    default String toJsonString() {
        return "JsonValue{" +
                "value=" + raw() +
                '}';
    }

}
