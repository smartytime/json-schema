package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Optional;

@FunctionalInterface
public interface SchemaValidator {
    SchemaValidator NOOP_VALIDATOR = NamedSchemaValidator.builder().name("NOOP").wrapped((subject, report) -> true).build();

    boolean validate(PathAwareJsonValue subject, ValidationReport report);

    default Optional<ValidationError> validate(PathAwareJsonValue subject) {
        throw new UnsupportedOperationException();
    }

    default Optional<ValidationError> validate(JsonValue subject) {
        throw new UnsupportedOperationException();
    }

    default Schema schema() {
        throw new UnsupportedOperationException();
    }
}
