package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.NumberSchema;

import javax.json.JsonNumber;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.JsonSchemaType.INTEGER;
import static io.dugnutt.jsonschema.six.JsonSchemaType.NUMBER;
import static javax.json.JsonValue.ValueType;

public class NumberSchemaValidator extends SchemaValidator<NumberSchema> {

    public NumberSchemaValidator(NumberSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(final JsonValue subject) {
        ValueType schemaType = subject.getValueType();
        if (schemaType != ValueType.NUMBER && schema.isRequiresNumber()) {
            return Optional.of(failure(NUMBER, subject));
        } else if (schemaType == ValueType.NUMBER) {
            JsonNumber num = (JsonNumber) subject;
            List<ValidationError> errors = new ArrayList<>();

            if (schema.isRequiresInteger() && !num.isIntegral()) {
                errors.add(failure(INTEGER, num));
            }

            double intSubject = num.doubleValue();
            checkMinimum(intSubject).ifPresent(errors::add);
            checkMaximum(intSubject).ifPresent(errors::add);
            checkMultipleOf(intSubject).ifPresent(errors::add);
            return errors.stream().findFirst();
        } else {
            return Optional.empty();
        }
    }

    private Optional<ValidationError> checkMaximum(final double subject) {
        Number maximum = schema.getMaximum();
        Number exclusiveMaximum = schema.getExclusiveMaximum();

        if (maximum != null && maximum.doubleValue() < subject) {
            return Optional.of(failure(subject + " is not lower or equal to " + maximum, JsonSchemaKeyword.MAXIMUM));
        }

        if (exclusiveMaximum != null && exclusiveMaximum.doubleValue() <= subject) {
            return Optional.of(failure(subject + " is not lower than " + maximum, JsonSchemaKeyword.EXCLUSIVE_MAXIMUM));
        }
        return Optional.empty();
    }

    private Optional<ValidationError> checkMinimum(final double subject) {
        Number minimum = schema.getMinimum();
        Number exclusiveMinimum = schema.getExclusiveMinimum();

        if (minimum != null && minimum.doubleValue() > subject) {
            return Optional.of(failure(subject + " is not higher or equal to " + minimum, JsonSchemaKeyword.MINIMUM));
        }

        if (exclusiveMinimum != null && exclusiveMinimum.doubleValue() >= subject) {
            return Optional.of(failure(subject + " is not higher than " + minimum, JsonSchemaKeyword.EXCLUSIVE_MINIMUM));
        }
        return Optional.empty();
    }

    private Optional<ValidationError> checkMultipleOf(final double subject) {
        Number multipleOf = schema.getMultipleOf();
        if (multipleOf != null) {
            BigDecimal remainder = BigDecimal.valueOf(subject).remainder(
                    BigDecimal.valueOf(multipleOf.doubleValue()));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                return Optional.of(failure(subject + " is not a multiple of " + multipleOf, JsonSchemaKeyword.MULTIPLE_OF));
            }
        }
        return Optional.empty();
    }
}
