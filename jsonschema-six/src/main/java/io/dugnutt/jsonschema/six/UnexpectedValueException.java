package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.utils.JsonUtils;

import javax.json.JsonPointer;
import javax.json.JsonValue;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UnexpectedValueException extends SchemaException {

    public UnexpectedValueException(JsonPointer path, JsonValue element, JsonValue.ValueType... wanted) {
        super(path.toString(), String.format("Found %s, but was expecting %s", element.getValueType().toString().toLowerCase(), Arrays.toString(wanted)));
    }

    public UnexpectedValueException(SchemaLocation schemaLocation, JsonValue element, JsonValue.ValueType... wanted) {
        this(schemaLocation.getJsonPointerFragment(), element, wanted);

    }
    public UnexpectedValueException(JsonPointerPath pointer, JsonValue element, Class<? extends JsonValue>... wanted) {
        this(pointer, element, Arrays.stream(wanted).map(JsonUtils::jsonTypeForClass).toArray(JsonValue.ValueType[]::new));
    }

    public UnexpectedValueException(JsonPointerPath pointer, JsonValue element, JsonValue.ValueType... wanted) {
        this(pointer.toURIFragment(), element, wanted);
    }

    public UnexpectedValueException(URI pointerURI, JsonValue element, JsonValue.ValueType... wanted) {
        super(pointerURI, String.format("Found %s, but was expecting %s", element.getValueType().toString().toLowerCase(),
                //Convert types to lowercase
                Arrays.stream(wanted)
                        .map(type->type.name().toLowerCase())
                        .collect(Collectors.joining(","))));
    }
}
