package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.EmptySchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import java.util.Optional;

@Deprecated
public class EmptySchemaValidator extends SchemaValidator<EmptySchema> {

    public static EmptySchemaValidator EMPTY_SCHEMA_VALIDATOR = new EmptySchemaValidator(EmptySchema.EMPTY_SCHEMA);

    public EmptySchemaValidator(EmptySchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
    }

    public EmptySchemaValidator(EmptySchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject) {
        return Optional.empty();
    }
}
