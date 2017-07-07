package org.everit.json;

public class MissingExpectedPropertyException extends RuntimeException {
    public MissingExpectedPropertyException() {
    }

    public MissingExpectedPropertyException(JsonObject source, String property) {
        super(String.format("Found %s, but was expecting %s", source.properties(), property));
    }
}
