package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.NumberSchema;
import org.everit.json.JsonElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NumberSchemaValidator extends SchemaValidator<NumberSchema> {

    public NumberSchemaValidator(NumberSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> subject) {
            if (subject.schemaType() != JsonSchemaType.Number && schema.requiresNumber()) {
                return Optional.of(failure(JsonSchemaType.Number, subject.schemaType()));
            } else if (subject.schemaType() != JsonSchemaType.Integer && schema.requiresInteger()) {
                return Optional.of(failure(JsonSchemaType.Integer, subject.schemaType()));
            } else {
                List<ValidationError> errors = new ArrayList<>();

                double intSubject = ((Number) subject).doubleValue();
                checkMinimum(intSubject).ifPresent(errors::add);
                checkMaximum(intSubject).ifPresent(errors::add);
                checkMultipleOf(intSubject).ifPresent(errors::add);
                return errors.stream().findFirst();
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
