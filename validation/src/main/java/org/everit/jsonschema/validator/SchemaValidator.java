package org.everit.jsonschema.validator;

import org.everit.json.JsonElement;
import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.Schema;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class SchemaValidator<S extends Schema> {

    protected final S schema;

    public SchemaValidator(S schema) {
        this.schema = checkNotNull(schema);
    }

    public S schema() {
        return schema;
    }

    public abstract Optional<ValidationError> validate(JsonElement<?>toBeValidated);

    protected ValidationError failure(String message, String keyword) {
        return new ValidationError(schema(), message, keyword, schema().getSchemaLocation());
    }

    protected ValidationError failure(JsonSchemaType expectedType, JsonSchemaType foundType) {
        String message = String.format("expected type: %s, found: %s", expectedType, foundType);
        return new ValidationError(schema(), message, "typeMismatch", schema().getSchemaLocation());
    }

}
