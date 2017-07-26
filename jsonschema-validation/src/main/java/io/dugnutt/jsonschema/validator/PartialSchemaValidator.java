package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import java.util.Optional;

public interface PartialSchemaValidator {
    default boolean appliesToSchema(JsonSchema schema) {
        return true;
    }

    default boolean appliesToValue(PathAwareJsonValue value) {
        return true;
    }

    Optional<ValidationError> validate(PathAwareJsonValue subject, JsonSchema schema, SchemaValidatorFactory factory);
}
