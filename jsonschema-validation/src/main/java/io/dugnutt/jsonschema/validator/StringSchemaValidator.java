package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.StringSchema;
import io.dugnutt.jsonschema.validator.formatValidators.FormatValidator;

import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.dugnutt.jsonschema.validator.ChainedValidator.firstCheck;

public class StringSchemaValidator extends SchemaValidator<StringSchema> {

    public StringSchemaValidator(StringSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue subject) {
        return firstCheck(subject, s -> verifyType(s, JsonSchemaType.STRING, schema.requiresString()))
                .thenIf(s -> subject.getValueType() == JsonValue.ValueType.STRING)
                .thenCheckAs(JsonString.class, s -> {
                    String stringSubject = s.getString();
                    List<ValidationError> allErrors = new ArrayList<>();
                    allErrors.addAll(testLength(stringSubject));
                    testPattern(stringSubject).ifPresent(allErrors::add);

                    getFormatValidator()
                            .map(validator -> validator.validate(stringSubject).orElse(null))
                            .map(error -> failure(error, "format"))
                            .ifPresent(allErrors::add);
                    return ValidationError.collectErrors(schema, allErrors);
                }).getError();
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
