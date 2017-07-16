package io.dugnutt.json;

import lombok.EqualsAndHashCode;

import javax.json.JsonString;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An immutable JSON string value.
 */
@EqualsAndHashCode
public class JsonStringImpl implements JsonString {
    private final String string;

    public JsonStringImpl(String string) {
        this.string = checkNotNull(string);
    }

    /**
     * Returns the JSON string value.
     *
     * @return a JSON string value
     */
    @Override
    public String getString() {
        return string;
    }

    /**
     * Returns the char sequence for the JSON String value
     *
     * @return a char sequence for the JSON String value
     */
    @Override
    public CharSequence getChars() {
        return string;
    }

    /**
     * Returns the value type of this JSON value.
     *
     * @return JSON value type
     */
    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }
}
