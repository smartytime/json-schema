package org.everit.jsonschema.validator;

import org.everit.json.JsonElement;
import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.StringSchema;
import org.everit.jsonschema.validator.internal.FormatValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class StringSchemaValidator extends SchemaValidator<StringSchema> {

    public StringSchemaValidator(StringSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> subject) {
        if (subject.schemaType() != JsonSchemaType.String && schema.requiresString()) {
            return Optional.of(failure(JsonSchemaType.String, subject.schemaType()));
        } else if (subject.schemaType() == JsonSchemaType.String) {
            String stringSubject = subject.coerceToString();
            List<ValidationError> allErrors = new ArrayList<>();
            allErrors.addAll(testLength(stringSubject));
            testPattern(stringSubject).ifPresent(allErrors::add);

            getFormatValidator()
                    .map(validator -> validator.validate(stringSubject).orElse(null))
                    .map(error -> failure(error, "format"))
                    .ifPresent(allErrors::add);
            return ValidationError.collectErrors(schema, allErrors);
        } else {
            return Optional.empty();
        }
    }

    private Optional<FormatValidator> getFormatValidator() {
        if (schema.getFormatType() != null) {
            return Optional.of(FormatValidator.forFormat(schema.getFormatType().toString()));
        }
        return Optional.empty();
    }

    private List<ValidationError> testLength(final String subject) {
        Integer minLength = schema.getMinLength();
        Integer maxLength = schema.getMaxLength();
        int actualLength = subject.codePointCount(0, subject.length());
        List<ValidationError> rval = new ArrayList<>();
        if (minLength != null && actualLength < minLength) {
            rval.add(failure("expected minLength: " + minLength + ", actual: "
                    + actualLength, "minLength"));
        }
        if (maxLength != null && actualLength > maxLength) {
            rval.add(failure("expected maxLength: " + maxLength + ", actual: "
                    + actualLength, "maxLength"));
        }
        return rval;
    }

    private Optional<ValidationError> testPattern(final String subject) {
        Pattern pattern = schema.getPattern();

        if (pattern != null && !pattern.matcher(subject).find()) {
            String message = String.format("string [%s] does not match pattern %s",
                    subject, pattern.pattern());
            return Optional.of(failure(message, "pattern"));
        }
        return Optional.empty();
    }
}
