package org.martysoft.jsonschema.validator;

import org.martysoft.jsonschema.v6.EmptySchema;

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
