package org.martysoft.jsonschema.validator;

import org.martysoft.jsonschema.v6.Schema;

import javax.json.JsonValue;
import java.util.Optional;

public class SchemaValidation {
    public static Optional<ValidationError> validate(JsonValue input, Schema schema) {
        final SchemaValidator<?> validator = SchemaValidatorFactory.findValidator(schema);
        return validator.validate(input);
    }
}
