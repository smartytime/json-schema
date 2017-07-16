package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Optional;

public class SchemaValidation {
    public static Optional<ValidationError> validate(JsonValue input, Schema schema) {
        final SchemaValidator<?> validator = SchemaValidatorFactory.createValidatorForSchema(schema);
        return validator.validate(input);
    }
}
