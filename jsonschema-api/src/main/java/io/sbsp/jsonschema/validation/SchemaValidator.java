package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;

import javax.json.JsonValue;
import java.util.Optional;

import static io.sbsp.jsonschema.JsonValueWithPath.*;

public interface SchemaValidator {

    boolean validate(JsonValueWithPath subject, ValidationReport report);

    default Optional<ValidationError> validate(JsonValue subject) {
        JsonValueWithPath pathAwareSubject = fromJsonValue(subject, subject, getSchema().getLocation());
        ValidationReport report = validate(pathAwareSubject);
        return ValidationError.collectErrors(getSchema(), pathAwareSubject.getPath(), report.getErrors());
    }

    default ValidationReport validate(JsonValueWithPath subject) {
        ValidationReport report = new ValidationReport();
        validate(subject, report);
        return report;
    }

    Schema getSchema();
}
