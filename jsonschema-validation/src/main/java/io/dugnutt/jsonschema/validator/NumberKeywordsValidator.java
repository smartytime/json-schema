package io.dugnutt.jsonschema.validator;

import com.google.common.base.Preconditions;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.NumberKeywords;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import javax.json.JsonNumber;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MULTIPLE_OF;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;
import static javax.json.JsonValue.ValueType;

public class NumberKeywordsValidator implements PartialSchemaValidator {

    public static NumberKeywordsValidator numberKeywordsValidator() {
        return new NumberKeywordsValidator();
    }

    @Override
    public boolean appliesToSchema(JsonSchema schema) {
        return schema.getNumberKeywords().isPresent();
    }

    @Override
    public boolean appliesToValue(PathAwareJsonValue value) {
        return value.is(ValueType.NUMBER);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject, JsonSchema schema, SchemaValidatorFactory factory) {
        Preconditions.checkArgument(subject.is(ValueType.NUMBER), "Requires JsonArray as input");
        NumberKeywords keywords = schema.getNumberKeywords()
                .orElseThrow(() -> new IllegalArgumentException("Schema must have number keywords"));

        Helper helper = new Helper(schema, keywords);

        List<ValidationError> errors = new ArrayList<>();
        JsonNumber jsonNumber = subject.asJsonNumber();
        double intSubject = jsonNumber.doubleValue();
        helper.checkMinimum(subject, intSubject).ifPresent(errors::add);
        helper.checkMaximum(subject, intSubject).ifPresent(errors::add);
        helper.checkMultipleOf(subject, intSubject).ifPresent(errors::add);
        return errors.stream().findFirst();
    }

    private static class Helper {
        private final JsonSchema schema;
        private final NumberKeywords keywords;

        public Helper(JsonSchema schema, NumberKeywords keywords) {
            this.schema = checkNotNull(schema);
            this.keywords = checkNotNull(keywords);
        }

        private Optional<ValidationError> checkMaximum(PathAwareJsonValue obj, final double subject) {
            Number maximum = keywords.getMaximum();
            Number exclusiveMaximum = keywords.getExclusiveMaximum();

            if (maximum != null && maximum.doubleValue() < subject) {
                return buildKeywordFailure(obj, schema, MAXIMUM)
                        .message("Value not lower or equal to %s", maximum)
                        .buildOptional();
            }

            if (exclusiveMaximum != null && exclusiveMaximum.doubleValue() <= subject) {
                return buildKeywordFailure(obj, schema, EXCLUSIVE_MAXIMUM)
                        .message("Value is not lower than %s", maximum)
                        .buildOptional();
            }
            return Optional.empty();
        }

        private Optional<ValidationError> checkMinimum(PathAwareJsonValue obj, final double subject) {
            Number minimum = keywords.getMinimum();
            Number exclusiveMinimum = keywords.getExclusiveMinimum();

            if (minimum != null && minimum.doubleValue() > subject) {
                return buildKeywordFailure(obj, schema, MINIMUM)
                        .message("Value is not higher or equal to %s", minimum)
                        .buildOptional();
            }

            if (exclusiveMinimum != null && exclusiveMinimum.doubleValue() >= subject) {
                return buildKeywordFailure(obj, schema, EXCLUSIVE_MINIMUM)
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
                    return buildKeywordFailure(obj, schema, MULTIPLE_OF)
                            .message("Value is not a multiple of %s", multipleOf)
                            .buildOptional();
                }
            }
            return Optional.empty();
        }
    }
}
