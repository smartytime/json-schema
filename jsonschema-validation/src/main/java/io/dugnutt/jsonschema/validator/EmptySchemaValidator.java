package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.EmptySchema;

import javax.json.JsonValue;
import java.util.Optional;

public class EmptySchemaValidator extends SchemaValidator<EmptySchema> {

    public static EmptySchemaValidator EMPTY_SCHEMA_VALIDATOR = new EmptySchemaValidator(EmptySchema.EMPTY_SCHEMA);

    public EmptySchemaValidator(EmptySchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue toBeValidated) {
        return Optional.empty();
    }
}
