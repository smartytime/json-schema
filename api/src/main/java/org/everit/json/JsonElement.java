package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;

public interface JsonElement<X> {

    default JsonArray<?> asArray() {
        if (this instanceof JsonArray) {
            return (JsonArray<?>) this;
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Array);
    }


    default JsonObject<?> asObject() {
        if (this instanceof JsonObject) {
            return (JsonObject<?>) this;
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Object);
    }


    default String asString() {
        if (schemaType() == JsonSchemaType.String) {
            return (String) raw();
        }
        throw new UnexpectedValueException(this, JsonSchemaType.String);
    }

    default Boolean asBoolean() {
        if (schemaType() == JsonSchemaType.Boolean) {
            return (Boolean) raw();
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Boolean);
    }

    default Number asNumber() {
        if (schemaType() == JsonSchemaType.Number) {
            return (Number) raw();
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Number);
    }

    default Integer asInteger() {
        if (schemaType() == JsonSchemaType.Number) {
            return (Integer) raw();
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Integer);
    }

    JsonSchemaType schemaType();

    Object raw();
    X unbox();

    JsonPath path();
}

