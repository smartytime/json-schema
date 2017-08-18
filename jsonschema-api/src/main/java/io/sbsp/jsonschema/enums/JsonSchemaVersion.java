package io.sbsp.jsonschema.enums;

public enum JsonSchemaVersion {
    Draft3,Draft4,Draft5,Draft6,Custom,Unknown;

    public boolean isBefore(JsonSchemaVersion otherVersion) {
        return this.compareTo(otherVersion) < 0;
    }

    public static JsonSchemaVersion latest() {
        return Draft6;
    }
}
