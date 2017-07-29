package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Optional;

@FunctionalInterface
public interface SchemaValidator {

    boolean validate(JsonValueWithLocation subject, ValidationReport report);

    /**
     * @deprecated Use validateWithReport
     */
    @Deprecated
    default Optional<ValidationError> validate(JsonValue subject) {
        JsonValueWithLocation pathAwareSubject = JsonValueWithLocation.fromJsonValue(subject, getSchema().getLocation());
        ValidationReport report = validate(pathAwareSubject);
        return ValidationError.collectErrors(getSchema(), pathAwareSubject.getPath(), report.getErrors());
    }

    default ValidationReport validate(JsonValueWithLocation subject) {
        ValidationReport report = new ValidationReport();
        validate(subject, report);
        return report;
    }


    default Schema getSchema() {
        throw new UnsupportedOperationException();
    }
}
