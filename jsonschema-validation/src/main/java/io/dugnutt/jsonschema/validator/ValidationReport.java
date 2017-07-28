package io.dugnutt.jsonschema.validator;

import com.google.common.collect.ImmutableListMultimap;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.dugnutt.jsonschema.validator.ValidationError.collectErrors;

public class ValidationReport {

    ImmutableListMultimap.Builder<String, ValidationError> allErrors = ImmutableListMultimap.builder();

    public boolean addError(ValidationError validationError) {
        String key = validationError.getPointerToViolation();
        allErrors.put(key, validationError);
        return false;
    }

    public boolean addReport(Schema schema, PathAwareJsonValue subject, JsonSchemaKeyword keyword, String message, ValidationReport report) {
        final List<ValidationError> errors = report.getErrors();
        if (errors.size() > 0) {
            addError(ValidationError.validationBuilder()
                    .schemaLocation(schema.getPointerFragmentURI())
                    .violatedSchema(schema)
                    .causingExceptions(errors)
                    .keyword(keyword)
                    .message(message)
                    .code("validation.keyword." + keyword.key())
                    .pointerToViolation(subject.getPath())
                    .build());
            return false;
        }
        return true;
    }

    public boolean addReport(Schema schema, PathAwareJsonValue subject, ValidationReport report) {
        Optional<ValidationError> error = collectErrors(schema, subject.getPath(), report.getErrors());
        error.ifPresent(this::addError);
        return !error.isPresent();
    }

    public List<ValidationError> getErrors() {
        return allErrors.build().values().asList();
    }

    public void log(SchemaValidator validator) {
        String key = validator.toString();
        counts.putIfAbsent(key, new AtomicInteger(0));
        counts.get(key).getAndIncrement();
    }

    public static final Map<String, AtomicInteger> counts = new HashMap<>();
}
