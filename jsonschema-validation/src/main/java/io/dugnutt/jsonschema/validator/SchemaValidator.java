package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Optional;

@FunctionalInterface
public interface SchemaValidator {

    boolean validate(PathAwareJsonValue subject, ValidationReport report);

    default Optional<ValidationError> validate(JsonValue subject) {
        PathAwareJsonValue pathAwareSubject = new PathAwareJsonValue(subject, getSchema().getLocation().getJsonPath());
        return validate(pathAwareSubject);
    }

    @Deprecated
    default Optional<ValidationError> validate(PathAwareJsonValue subject) {
        ValidationReport report = new ValidationReport();
        validate(subject, report);
        return ValidationError.collectErrors(getSchema(), subject.getPath(), report.getErrors());
    }


    default Schema getSchema() {
        throw new UnsupportedOperationException();
    }
}
