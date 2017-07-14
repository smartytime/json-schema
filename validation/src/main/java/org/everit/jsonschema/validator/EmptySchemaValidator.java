package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.EmptySchema;

import javax.json.JsonValue;
import java.util.Optional;

public class EmptySchemaValidator extends SchemaValidator<EmptySchema> {
    public EmptySchemaValidator(EmptySchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue toBeValidated) {
        return Optional.empty();
    }

}
