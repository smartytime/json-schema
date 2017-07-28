package io.dugnutt.jsonschema.six;

import javax.json.JsonValue;

import static com.google.common.base.Preconditions.checkNotNull;

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

    public static JsonSchemaType fromJsonType(JsonValue.ValueType type) {
        checkNotNull(type, "type must not be null");
        return valueOf(type.name());
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
