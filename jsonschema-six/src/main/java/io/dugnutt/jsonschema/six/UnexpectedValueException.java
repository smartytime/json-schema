package io.dugnutt.jsonschema.six;

import javax.json.JsonValue;
import java.util.Arrays;

public class UnexpectedValueException extends SchemaException {
    public UnexpectedValueException(String message) {
        super(null, message);
    }

    public UnexpectedValueException(JsonValue element, JsonValue.ValueType... wanted) {
        super(null, String.format("Found %s, but was expecting %s", element.getValueType().toString().toLowerCase(), Arrays.toString(wanted)));
    }

    public UnexpectedValueException(JsonPointerPath pointer, JsonValue element, JsonValue.ValueType... wanted) {
        super(pointer.toURIFragment(), String.format("Found %s, but was expecting %s", element.getValueType().toString().toLowerCase(), Arrays.toString(wanted)));
    }
}
