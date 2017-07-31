package io.dugnutt.jsonschema.six;

import javax.json.JsonValue;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UnexpectedValueException extends SchemaException {

    public UnexpectedValueException(JsonPath path, JsonValue element, JsonValue.ValueType... wanted) {
        this(path.toURIFragment(), element, wanted);
    }

    public UnexpectedValueException(SchemaLocation location, JsonValue element, JsonValue.ValueType... wanted) {
        this(location.getJsonPointerFragment(), element, wanted);
    }

    public UnexpectedValueException(URI uriFragment, JsonValue element, JsonValue.ValueType... wanted) {
        super(uriFragment, String.format("Found %s, but was expecting %s", element.getValueType().toString().toLowerCase(),
                //Convert keywords to lowercase
                Arrays.stream(wanted)
                        .map(type -> type.name().toLowerCase())
                        .collect(Collectors.joining(","))));
    }
}
