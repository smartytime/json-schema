package org.everit.jsonschema.api;

import javax.json.JsonValue;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkNotNull;

public enum JsonSchemaType {
    String,
    Boolean,
    Number,
    Integer,
    Null,
    Object,
    Array;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static JsonSchemaType fromJsonType(JsonValue.ValueType type) {
        checkNotNull(type, "type must not be null");
        return valueOf(UPPER_UNDERSCORE.to(UPPER_CAMEL, type.name()));
    }
}
