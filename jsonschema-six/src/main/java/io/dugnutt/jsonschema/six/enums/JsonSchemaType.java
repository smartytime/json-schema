package io.dugnutt.jsonschema.six.enums;

import io.dugnutt.jsonschema.six.SchemaException;

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
