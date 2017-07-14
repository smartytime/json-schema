package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;

import javax.json.JsonArray;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public interface JsonElement<X> {

    default JsonArray<?> asArray() {
        if (this instanceof JsonArray) {
            return (JsonArray<?>) this;
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Array);
    }

    default boolean isNumber() {
        JsonSchemaType schemaType = schemaType();
        return schemaType == JsonSchemaType.Integer || schemaType == JsonSchemaType.Number;
    }
    
    default boolean isAnyOf(JsonSchemaType... any) {
        checkNotNull(any, "any must not be null");
        JsonSchemaType schemaType = schemaType();
        return Arrays.stream(any).anyMatch(schemaType::equals);
    }
    
    

    default Boolean asBoolean() {
        if (schemaType() == JsonSchemaType.Boolean) {
            return (Boolean) raw();
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Boolean);
    }

    default Integer asInt() {
        if (schemaType() == JsonSchemaType.Integer) {
            return ((Number) raw()).intValue();
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Integer);
    }

    default Number asNumber() {
        if (schemaType() == JsonSchemaType.Number || schemaType() == JsonSchemaType.Integer) {
            return (Number) raw();
        }
        throw new UnexpectedValueException(this, JsonSchemaType.Number);
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

    default String coerceToString() {
        JsonSchemaType schemaType = schemaType();
        if(schemaType == JsonSchemaType.Object || schemaType ==JsonSchemaType.Array) {
            throw new UnexpectedValueException(this, JsonSchemaType.String);
        } else if (schemaType == JsonSchemaType.Null) {
            return null;
        } else  {
            return raw().toString();
        }
    }

    JsonPath path();

    Object raw();

    JsonSchemaType schemaType();

    X unbox();
}

