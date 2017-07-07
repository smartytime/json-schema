package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;

import java.util.Arrays;

public class UnexpectedValueException extends RuntimeException {
    public UnexpectedValueException(String message) {
        super(message);
    }

    public UnexpectedValueException(Class<?> found, Class<?> expected) {
        super(String.format("Found %s, but was expecting %s", found, expected));
    }

    public UnexpectedValueException(JsonElement<?> element, JsonSchemaType... wanted) {
        super(String.format("Found %s, but was expecting %s", element.type(), Arrays.toString(wanted)));
    }
}
