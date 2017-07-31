package io.sbsp.jsonschema.six.enums;

import io.sbsp.jsonschema.six.SchemaException;

/**
 * Represents the valid json-schema types.
 */
public enum JsonSchemaType {
    STRING,
    BOOLEAN,
    NUMBER,
    INTEGER,
    NULL,
    OBJECT,
    ARRAY;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static JsonSchemaType fromString(String type) {
        if (type == null) {
            return NULL;
        }
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SchemaException("Invalid schema type:" + type);
        }
    }

}
