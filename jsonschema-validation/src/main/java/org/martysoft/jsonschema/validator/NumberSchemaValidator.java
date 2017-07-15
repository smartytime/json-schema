package org.martysoft.jsonschema.validator;

import org.martysoft.jsonschema.v6.NumberSchema;

import javax.json.JsonNumber;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.json.JsonValue.ValueType;
import static org.martysoft.jsonschema.v6.JsonSchemaType.NUMBER;

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
        Boolean exclusiveMaximum = schema.isExclusiveMaximum();

        if (maximum != null) {
            if (exclusiveMaximum && maximum.doubleValue() <= subject) {
                return Optional.of(failure(subject + " is not lower than " + maximum, "exclusiveMaximum"));
            } else if (maximum.doubleValue() < subject) {
                return Optional.of(failure(subject + " is not lower or equal to " + maximum, "maximum"));
            }
        }
        return Optional.empty();
    }

    private Optional<ValidationError> checkMinimum(final double subject) {
        Number minimum = schema.getMinimum();
        Boolean exclusiveMinimum = schema.isExclusiveMinimum();

        if (minimum != null) {
            if (exclusiveMinimum && subject <= minimum.doubleValue()) {
                return Optional.of(failure(subject + " is not higher than " + minimum, "exclusiveMinimum"));
            } else if (subject < minimum.doubleValue()) {
                return Optional.of(failure(subject + " is not higher or equal to " + minimum, "minimum"));
            }
        }
        return Optional.empty();
    }

    private Optional<ValidationError> checkMultipleOf(final double subject) {
        Number multipleOf = schema.getMultipleOf();
        if (multipleOf != null) {
            BigDecimal remainder = BigDecimal.valueOf(subject).remainder(
                    BigDecimal.valueOf(multipleOf.doubleValue()));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                return Optional.of(failure(subject + " is not a multiple of " + multipleOf, "multipleOf"));
            }
        }
        return Optional.empty();
    }
}
