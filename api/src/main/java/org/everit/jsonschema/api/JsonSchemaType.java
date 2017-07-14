package org.everit.jsonschema.api;

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
}
