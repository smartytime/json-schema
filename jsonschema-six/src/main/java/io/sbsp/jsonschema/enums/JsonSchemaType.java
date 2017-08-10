package io.sbsp.jsonschema.enums;

import io.sbsp.jsonschema.SchemaException;

import javax.json.JsonValue;
import java.util.EnumSet;
import java.util.Set;

import static javax.json.JsonValue.*;

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

    public Set<JsonValue.ValueType> appliesTo() {
        switch(this) {
            case INTEGER:
                return EnumSet.of(ValueType.NUMBER);
            case BOOLEAN:
                return EnumSet.of(ValueType.TRUE, ValueType.FALSE);
            case STRING:
                return EnumSet.of(ValueType.STRING);
            case NUMBER:
                return EnumSet.of(ValueType.NUMBER);
            case NULL:
                return EnumSet.of(ValueType.NULL);
            case OBJECT:
                return EnumSet.of(ValueType.OBJECT);
            case ARRAY:
                return EnumSet.of(ValueType.ARRAY);
            default:
                throw new IllegalStateException("Unable to determine types");
        }
    }

}
