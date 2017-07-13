package org.everit.jsonschema.validator;

import org.everit.json.JsonElement;
import org.everit.jsonschema.api.CombinedSchema;
import org.everit.jsonschema.api.Schema;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class CombinedSchemaValidator extends SchemaValidator<CombinedSchema> {

    public CombinedSchemaValidator(CombinedSchema schema) {
        super(schema);
    }

    /**
     * Validation criterion.
     */
    @FunctionalInterface
    public interface ValidationCriterion {

        /**
         * Throws a {@link ValidationError} if the implemented criterion is not fulfilled by the
         * {@code subschemaCount} and the {@code matchingSubschemaCount}.
         *
         * @param subschemaCount         the total number of checked subschemas
         * @param matchingSubschemaCount the number of subschemas which successfully validated the subject (did not throw
         *                               {@link ValidationError})
         */
        Optional<ValidationError> validate(int subschemaCount, int matchingSubschemaCount);
    }

    public ValidationCriterion getCriterion() {
        switch (schema.getCombinedSchemaType()) {
            case AllOf:
                return ALL_CRITERION;
            case AnyOf:
                return ANY_CRITERION;
            case OneOf:
                return ONE_CRITERION;
            default:
                throw new IllegalStateException("Unable to determine combined schema type: " + schema.getCombinedSchemaType());
        }
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> toBeValidated) {
        Collection<Schema> subschemas = schema.getSubSchemas();
        CombinedSchemaValidator.ValidationCriterion criterion = this.getCriterion();
        List<ValidationError> failures = subschemas.stream()
                .map(schema -> SchemaValidatorFactory
                        .findValidator(schema)
                        .validate(toBeValidated)
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        int matchingCount = subschemas.size() - failures.size();

        return criterion.validate(subschemas.size(), matchingCount)
                .map(e -> new ValidationError(schema,
                        new StringBuilder(e.getPointerToViolation()),
                        e.getMessage(),
                        failures,
                        e.getKeyword(),
                        schema.getSchemaLocation()));
    }

    /**
     * Validation criterion for {@code allOf} schemas.
     */
    public static final ValidationCriterion ALL_CRITERION = new ValidationCriterion() {

        @Override
        public String toString() {
            return "allOf";
        }

        @Override
        public Optional<ValidationError> validate(int subschemaCount, int matchingCount) {
            if (matchingCount < subschemaCount) {
                return Optional.of(new ValidationError(null,
                        format("only %d subschema matches out of %d", matchingCount, subschemaCount),
                        "allOf"
                ));
            }
            return Optional.empty();
        }
    };
    /**
     * Validation criterion for {@code anyOf} schemas.
     */
    public static final ValidationCriterion ANY_CRITERION = new ValidationCriterion() {

        @Override
        public Optional<ValidationError> validate(int subschemaCount, int matchingCount) {
            if (matchingCount == 0) {
                return Optional.of(new ValidationError(null, format(
                        "no subschema matched out of the total %d subschemas",
                        subschemaCount), "anyOf"));
            }
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "anyOf";
        }
    };
    /**
     * Validation criterion for {@code oneOf} schemas.
     */
    public static final ValidationCriterion ONE_CRITERION =
            new ValidationCriterion() {

                @Override
                public Optional<ValidationError> validate(int subschemaCount, int matchingCount) {
                    if (matchingCount != 1) {
                        return Optional.of(new ValidationError(null, format("%d subschemas matched instead of one",
                                matchingCount), "oneOf"));
                    }
                    return Optional.empty();
                }

                @Override
                public String toString() {
                    return "oneOf";
                }
            };



}
