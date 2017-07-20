package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.StringSchema;

import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.FORMAT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PATTERN;

public class StringSchemaValidator extends SchemaValidator<StringSchema> {
    public StringSchemaValidator(StringSchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
    }

    public StringSchemaValidator(StringSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject) {
        if(!subject.is(JsonValue.ValueType.STRING) && schema.requiresString()) {
            return buildTypeMismatchError(subject, JsonSchemaType.STRING).buildOptional();
        } else if (subject.is(JsonValue.ValueType.STRING)) {
            String stringSubject = subject.asString();
            List<ValidationError> allErrors = new ArrayList<>();

            //Test the string's length
            testLength(subject, stringSubject).forEach(allErrors::add);

            //Test the pattern
            testPattern(subject, stringSubject).ifPresent(allErrors::add);

            factory.getFormatValidator(schema.getFormat())
                    .map(stringValidator -> stringValidator.validate(stringSubject).orElse(null))
                    .map(error -> buildKeywordFailure(subject, FORMAT)
                            .message(error).build())
                    .ifPresent(allErrors::add);
            return ValidationError.collectErrors(schema, subject.getPath(), allErrors);
        }
        return Optional.empty();
    }

    private List<ValidationError> testLength(PathAwareJsonValue subject, final String string) {
        Integer minLength = schema.getMinLength();
        Integer maxLength = schema.getMaxLength();
        int actualLength = string.codePointCount(0, string.length());
        List<ValidationError> errors = new ArrayList<>();
        if (minLength != null && actualLength < minLength) {
            buildKeywordFailure(subject, MIN_LENGTH)
                    .message("expected minLength: %d, actual: %d", minLength, actualLength)
                    .addToErrorList(errors);
        }
        if (maxLength != null && actualLength > maxLength) {
            buildKeywordFailure(subject, MAX_LENGTH)
                    .message("expected maxLength: %d, actual: %d", maxLength, actualLength)
                    .addToErrorList(errors);
        }
        return errors;
    }

    private Optional<ValidationError> testPattern(PathAwareJsonValue subject, final String string) {
        Pattern pattern = schema.getPattern();
        if (pattern != null && !pattern.matcher(string).find()) {
            return buildKeywordFailure(subject, PATTERN)
                    .message("string [%s] does not match pattern %s", string, pattern.pattern())
                    .buildOptional();
        }
        return Optional.empty();
    }
}
