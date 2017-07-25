package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.NumberKeywords;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import javax.json.JsonNumber;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MULTIPLE_OF;
import static javax.json.JsonValue.ValueType;

public class NumberKeywordsValidator extends KeywordValidator<NumberKeywords> {

    public NumberKeywordsValidator(JsonSchema schema, NumberKeywords keywords) {
        super(schema, keywords);
    }

    public NumberKeywordsValidator(JsonSchema schema, NumberKeywords keywords, SchemaValidatorFactory factory) {
        super(schema, keywords, factory);
    }

    @Override
    public Optional<ValidationError> validate(final PathAwareJsonValue subject) {
        if (!subject.is(ValueType.NUMBER)) {
            throw new IllegalArgumentException("Bad input.  Must be a JsonNumber instance");
        }
        List<ValidationError> errors = new ArrayList<>();
        JsonNumber jsonNumber = subject.asJsonNumber();
        double intSubject = jsonNumber.doubleValue();
        checkMinimum(subject, intSubject).ifPresent(errors::add);
        checkMaximum(subject, intSubject).ifPresent(errors::add);
        checkMultipleOf(subject, intSubject).ifPresent(errors::add);
        return errors.stream().findFirst();
    }

    private Optional<ValidationError> checkMaximum(PathAwareJsonValue obj, final double subject) {
        Number maximum = keywords.getMaximum();
        Number exclusiveMaximum = keywords.getExclusiveMaximum();

        if (maximum != null && maximum.doubleValue() < subject) {
            return buildKeywordFailure(obj, MAXIMUM)
                    .message("Value not lower or equal to %s", maximum)
                    .buildOptional();
        }

        if (exclusiveMaximum != null && exclusiveMaximum.doubleValue() <= subject) {
            return buildKeywordFailure(obj, EXCLUSIVE_MAXIMUM)
                    .message("Value is not lower than %s", maximum)
                    .buildOptional();
        }
        return Optional.empty();
    }

    private Optional<ValidationError> checkMinimum(PathAwareJsonValue obj, final double subject) {
        Number minimum = keywords.getMinimum();
        Number exclusiveMinimum = keywords.getExclusiveMinimum();

        if (minimum != null && minimum.doubleValue() > subject) {
            return buildKeywordFailure(obj, MINIMUM)
                    .message("Value is not higher or equal to %s", minimum)
                    .buildOptional();
        }

        if (exclusiveMinimum != null && exclusiveMinimum.doubleValue() >= subject) {
            return buildKeywordFailure(obj, EXCLUSIVE_MINIMUM)
                    .message("Value is not higher than %s", minimum)
                    .buildOptional();
        }
        return Optional.empty();
    }

    private Optional<ValidationError> checkMultipleOf(PathAwareJsonValue obj, final double subject) {
        Number multipleOf = keywords.getMultipleOf();
        if (multipleOf != null) {
            BigDecimal remainder = BigDecimal.valueOf(subject).remainder(
                    BigDecimal.valueOf(multipleOf.doubleValue()));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                return buildKeywordFailure(obj, MULTIPLE_OF)
                        .message("Value is not a multiple of %s", multipleOf)
                        .buildOptional();
            }
        }
        return Optional.empty();
    }
}
