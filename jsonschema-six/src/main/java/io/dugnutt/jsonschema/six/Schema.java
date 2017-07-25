package io.dugnutt.jsonschema.six;

public interface Schema {
    JsonSchemaGenerator toJson(final JsonSchemaGenerator writer);
}
