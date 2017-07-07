package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.EmptySchema;
import org.everit.json.JsonElement;

import java.util.Optional;

public class EmptySchemaValidator extends SchemaValidator<EmptySchema> {
    public EmptySchemaValidator(EmptySchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> toBeValidated) {
        return Optional.empty();
    }

}
