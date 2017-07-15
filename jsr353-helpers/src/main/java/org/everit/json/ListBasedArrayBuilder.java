package org.everit.json;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListBasedArrayBuilder implements JsonArrayBuilder {

    private final List<JsonValue> internal;

    public ListBasedArrayBuilder(List<JsonValue> internal) {
        this.internal = checkNotNull(internal);
    }

    public ListBasedArrayBuilder() {
        this(new ArrayList<>());
    }

    /**
     * Adds a value to the array.
     *
     * @param value the JSON value
     * @return this array builder
     * @throws NullPointerException if the specified value is null
     */
    @Override
    public JsonArrayBuilder add(JsonValue value) {
        return internalAdd(value);
    }

    /**
     * Adds a value to the array as a {@link JsonString}.
     *
     * @param value the string value
     * @return this array builder
     * @throws NullPointerException if the specified value is null
     */
    @Override
    public JsonArrayBuilder add(String value) {
        return internalAdd(value);
    }

    /**
     * Adds a value to the array as a {@link JsonNumber}.
     *
     * @param value the number value
     * @return this array builder
     * @throws NullPointerException if the specified value is null
     * @see JsonNumber
     */
    @Override
    public JsonArrayBuilder add(BigDecimal value) {
        return internalAdd(value);
    }

    /**
     * Adds a value to the array as a {@link JsonNumber}.
     *
     * @param value the number value
     * @return this array builder
     * @throws NullPointerException if the specified value is null
     * @see JsonNumber
     */
    @Override
    public JsonArrayBuilder add(BigInteger value) {
        return internalAdd(value);
    }

    /**
     * Adds a value to the array as a {@link JsonNumber}.
     *
     * @param value the number value
     * @return this array builder
     * @see JsonNumber
     */
    @Override
    public JsonArrayBuilder add(int value) {
        return internalAdd(value);
    }

    /**
     * Adds a value to the array as a {@link JsonNumber}.
     *
     * @param value the number value
     * @return this array builder
     * @see JsonNumber
     */
    @Override
    public JsonArrayBuilder add(long value) {
        return internalAdd(value);
    }

    /**
     * Adds a value to the array as a {@link JsonNumber}.
     *
     * @param value the number value
     * @return this array builder
     * @throws NumberFormatException if the value is Not-a-Number (NaN) or
     *                               infinity
     * @see JsonNumber
     */
    @Override
    public JsonArrayBuilder add(double value) {
        return internalAdd(value);
    }

    /**
     * Adds a {@link JsonValue#TRUE}  or {@link JsonValue#FALSE} value to the
     * array.
     *
     * @param value the boolean value
     * @return this array builder
     */
    @Override
    public JsonArrayBuilder add(boolean value) {
        return internalAdd(value);
    }

    /**
     * Adds a {@link JsonValue#NULL} value to the array.
     *
     * @return this array builder
     */
    @Override
    public JsonArrayBuilder addNull() {
        return internalAdd(JsonValue.NULL);
    }

    /**
     * Adds a {@link JsonObject} from an object builder to the array.
     *
     * @param builder the object builder
     * @return this array builder
     * @throws NullPointerException if the specified builder is null
     */
    @Override
    public JsonArrayBuilder add(JsonObjectBuilder builder) {
        return internalAdd(builder.build());
    }

    /**
     * Adds a {@link JsonArray} from an array builder to the array.
     *
     * @param builder the array builder
     * @return this array builder
     * @throws NullPointerException if the specified builder is null
     */
    @Override
    public JsonArrayBuilder add(JsonArrayBuilder builder) {
        return internalAdd(builder.build());
    }

    /**
     * Internally adds and wraps this entity.
     *
     * @param o The object to be added.  Can't be null
     * @return self-reference for chaining
     */
    private ListBasedArrayBuilder internalAdd(JsonValue o) {
        checkNotNull(o, "o must not be null");
        internal.add(o);
        return this;
    }

    private ListBasedArrayBuilder internalAdd(Number o) {
        checkNotNull(o, "o must not be null");
        internal.add(new JsonNumberImpl(o));
        return this;
    }

    private ListBasedArrayBuilder internalAdd(String o) {
        checkNotNull(o, "o must not be null");
        internal.add(new JsonStringImpl(o));
        return this;
    }

    private ListBasedArrayBuilder internalAdd(boolean value) {
        checkNotNull(value, "value must not be null");
        internal.add(value ? JsonValue.TRUE : JsonValue.FALSE);
        return this;
    }

    @Override
    public JsonArray build() {
        return new JsonArrayImpl(internal);
    }
}
