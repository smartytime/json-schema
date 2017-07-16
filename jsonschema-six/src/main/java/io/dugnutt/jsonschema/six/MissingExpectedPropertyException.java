package io.dugnutt.jsonschema.six;

import javax.json.JsonObject;

public class MissingExpectedPropertyException extends RuntimeException {
    public MissingExpectedPropertyException() {
    }

    public MissingExpectedPropertyException(JsonObject source, String property) {
        super(String.format("Found %s, but was expecting %s", source.keySet(), property));
    }
}
