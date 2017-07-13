package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;

import java.util.Arrays;

public class UnexpectedValueException extends RuntimeException {
    public UnexpectedValueException(String message) {
        super(message);
    }

    public UnexpectedValueException(JsonElement<?> element, JsonSchemaType... wanted) {
        super(String.format("Found %s, but was expecting %s", element.schemaType(), Arrays.toString(wanted)));
    }
}
