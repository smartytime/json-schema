package org.martysoft.jsonschema.v6;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

import static org.martysoft.jsonschema.v6.JsonSchemaProperty.EXCLUSIVE_MAXIMUM;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.EXCLUSIVE_MINIMUM;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.MAXIMUM;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.MINIMUM;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.MULTIPLE_OF;

/**
 * Number schema validator.
 */
@Getter
@Builder(toBuilder = true, builderClassName = "Builder", builderMethodName = "numberSchemaBuilder")
public class NumberSchema extends Schema {

    private final boolean requiresNumber;
    private final Number minimum;
    private final Number maximum;
    private final Number multipleOf;
    private final boolean exclusiveMinimum;
    private final boolean exclusiveMaximum;

    public NumberSchema() {
        this(builder());
    }

    /**
     * Constructor.
     *
     * @param builder the builder object containing validation criteria
     */
    public NumberSchema(final Builder builder) {
        super(builder);
        this.minimum = builder.minimum;
        this.maximum = builder.maximum;
        this.exclusiveMinimum = builder.exclusiveMinimum;
        this.exclusiveMaximum = builder.exclusiveMaximum;
        this.multipleOf = builder.multipleOf;
        this.requiresNumber = builder.requiresNumber;
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof NumberSchema) {
            NumberSchema that = (NumberSchema) o;
            return that.canEqual(this) &&
                    requiresNumber == that.requiresNumber &&
                    exclusiveMinimum == that.exclusiveMinimum &&
                    exclusiveMaximum == that.exclusiveMaximum &&
                    Objects.equals(minimum, that.minimum) &&
                    Objects.equals(maximum, that.maximum) &&
                    Objects.equals(multipleOf, that.multipleOf) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected void propertiesToJson(JsonSchemaGenerator writer) {
        writer.writeType(JsonSchemaType.NUMBER, requiresNumber)
                .optionalWrite(MINIMUM, minimum)
                .optionalWrite(MAXIMUM, maximum)
                .optionalWrite(MULTIPLE_OF, multipleOf)
                .writeIfTrue(EXCLUSIVE_MINIMUM, exclusiveMinimum)
                .writeIfTrue(EXCLUSIVE_MAXIMUM, exclusiveMinimum);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(super.hashCode(), requiresNumber, minimum, maximum, multipleOf, exclusiveMinimum, exclusiveMaximum);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NumberSchema;
    }

    /**
     * Builder class for {@link NumberSchema}.
     */
    public static class Builder extends Schema.Builder<NumberSchema> {

        private boolean requiresNumber = true;

        @Override
        public NumberSchema build() {
            return new NumberSchema(this);
        }

        public Builder exclusiveMaximum(final boolean exclusiveMaximum) {
            this.exclusiveMaximum = exclusiveMaximum;
            return this;
        }

        public Builder exclusiveMinimum(final boolean exclusiveMinimum) {
            this.exclusiveMinimum = exclusiveMinimum;
            return this;
        }

        public Builder maximum(final Number maximum) {
            this.maximum = maximum;
            return this;
        }

        public Builder minimum(final Number minimum) {
            this.minimum = minimum;
            return this;
        }

        public Builder multipleOf(final Number multipleOf) {
            this.multipleOf = multipleOf;
            return this;
        }

        public Builder requiresNumber(final boolean requiresNumber) {
            this.requiresNumber = requiresNumber;
            return this;
        }

    }
}
