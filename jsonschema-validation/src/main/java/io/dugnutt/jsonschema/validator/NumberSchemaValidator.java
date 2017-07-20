package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.NumberSchema;
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
import static io.dugnutt.jsonschema.six.JsonSchemaType.INTEGER;
import static io.dugnutt.jsonschema.six.JsonSchemaType.NUMBER;
import static javax.json.JsonValue.ValueType;

public class NumberSchemaValidator extends SchemaValidator<NumberSchema> {

    public NumberSchemaValidator(NumberSchema schema) {
        super(schema);
    }

    public NumberSchemaValidator(NumberSchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
    }

    @Override
    public Optional<ValidationError> validate(final PathAwareJsonValue subject) {
        if (!subject.is(ValueType.NUMBER) && schema.isRequiresNumber()) {
            return buildTypeMismatchError(subject, NUMBER).buildOptional();
        } else if (subject.is(ValueType.NUMBER)) {
            List<ValidationError> errors = new ArrayList<>();

            JsonNumber jsonNumber = subject.asJsonNumber();
            if (schema.isRequiresInteger() && !jsonNumber.isIntegral()) {
                errors.add(buildTypeMismatchError(subject, INTEGER).build());
            }

            double intSubject = jsonNumber.doubleValue();
            checkMinimum(subject, intSubject).ifPresent(errors::add);
            checkMaximum(subject, intSubject).ifPresent(errors::add);
            checkMultipleOf(subject, intSubject).ifPresent(errors::add);
            return errors.stream().findFirst();
        } else {
            return Optional.empty();
        }
    }

    private Optional<ValidationError> checkMaximum(PathAwareJsonValue obj, final double subject) {
        Number maximum = schema.getMaximum();
        Number exclusiveMaximum = schema.getExclusiveMaximum();

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
        Number minimum = schema.getMinimum();
        Number exclusiveMinimum = schema.getExclusiveMinimum();

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
        Number multipleOf = schema.getMultipleOf();
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
