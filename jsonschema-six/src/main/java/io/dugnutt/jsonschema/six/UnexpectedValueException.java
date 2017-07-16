package io.dugnutt.jsonschema.six;

import javax.json.JsonValue;
import java.util.Arrays;

public class UnexpectedValueException extends RuntimeException {
    public UnexpectedValueException(String message) {
        super(message);
    }

    public UnexpectedValueException(JsonValue element, JsonValue.ValueType... wanted) {
        super(String.format("Found %s, but was expecting %s", element.getValueType().toString().toLowerCase(), Arrays.toString(wanted)));
    }
}
