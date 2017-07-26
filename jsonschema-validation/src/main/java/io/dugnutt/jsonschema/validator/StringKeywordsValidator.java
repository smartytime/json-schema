package io.dugnutt.jsonschema.validator;

import com.google.common.base.Preconditions;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.StringKeywords;

import javax.json.JsonValue.ValueType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.FORMAT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PATTERN;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringKeywordsValidator implements PartialSchemaValidator {

    public static StringKeywordsValidator stringKeywordsValidator() {
        return new StringKeywordsValidator();
    }

    @Override
    public boolean appliesToSchema(JsonSchema schema) {
        checkNotNull(schema, "schema must not be null");
        return schema.getStringKeywords().isPresent();
    }

    @Override
    public boolean appliesToValue(PathAwareJsonValue value) {
        return value.is(ValueType.STRING);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject, JsonSchema schema, SchemaValidatorFactory factory) {
        Preconditions.checkArgument(subject.is(ValueType.STRING), "Requires JsonArray as input");
        StringKeywords keywords = schema.getStringKeywords()
                .orElseThrow(() -> new IllegalArgumentException("Schema must have string keywords"));

        String stringSubject = subject.asString();
        List<ValidationError> allErrors = new ArrayList<>();

        //Test the string's length
        testLength(subject, schema, keywords, stringSubject).forEach(allErrors::add);

        //Test the pattern
        keywords.findPattern().ifPresent(pattern->{
            if(!patternMatches(pattern, stringSubject)) {
                allErrors.add(
                        buildKeywordFailure(subject, schema, PATTERN)
                                .message("string [%s] does not match pattern %s", stringSubject, pattern.pattern())
                                .build()
                );
            }
        });

        // Test custom format validators
        factory.getFormatValidator(keywords.getFormat())
                .map(stringValidator -> stringValidator.validate(stringSubject).orElse(null))
                .map(error -> buildKeywordFailure(subject, schema, FORMAT)
                        .message(error).build())
                .ifPresent(allErrors::add);
        return ValidationError.collectErrors(schema, subject.getPath(), allErrors);
    }

    private List<ValidationError> testLength(PathAwareJsonValue subject, JsonSchema schema, StringKeywords keywords, final String string) {
        Integer minLength = keywords.getMinLength();
        Integer maxLength = keywords.getMaxLength();
        int actualLength = string.codePointCount(0, string.length());
        List<ValidationError> errors = new ArrayList<>();
        if (minLength != null && actualLength < minLength) {
            buildKeywordFailure(subject, schema, MIN_LENGTH)
                    .message("expected minLength: %d, actual: %d", minLength, actualLength)
                    .addToErrorList(errors);
        }
        if (maxLength != null && actualLength > maxLength) {
            buildKeywordFailure(subject, schema, MAX_LENGTH)
                    .message("expected maxLength: %d, actual: %d", maxLength, actualLength)
                    .addToErrorList(errors);
        }
        return errors;
    }

    private boolean patternMatches(Pattern pattern, final String string) {
        return pattern.matcher(string).find();
    }
}
