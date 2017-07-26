package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import java.util.Optional;

public interface PartialSchemaValidator {
    default boolean appliesToSchema(Schema schema) {
        return true;
    }

    default boolean appliesToValue(PathAwareJsonValue value) {
        return true;
    }

    PartialSchemaValidator forSchema(Schema schema, SchemaValidatorFactory factory);

    Optional<ValidationError> validate(PathAwareJsonValue subject, Schema schema, SchemaValidatorFactory factory);
}
