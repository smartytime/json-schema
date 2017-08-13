package io.sbsp.jsonschema;

import javax.json.JsonObject;

public class MissingExpectedPropertyException extends RuntimeException {

    public MissingExpectedPropertyException(JsonObject source, String property) {
        super(String.format("Found %s, but was expecting %s", source.keySet(), property));
    }
}
