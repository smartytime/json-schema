package io.dugnutt.jsonschema.six;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Min;
import java.util.Objects;

/**
 * Number schema validator.
 */
@Getter
@Builder(toBuilder = true, builderClassName = "Builder", builderMethodName = "numberSchemaBuilder")
public class NumberSchema extends Schema {

    private final boolean requiresNumber;
    private final Number minimum;
    private final Number maximum;

    @Min(1)
    private final Number multipleOf;
    private final Number exclusiveMinimum;
    private final Number exclusiveMaximum;

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
    public int hashCode() {
        return Objects
                .hash(super.hashCode(), requiresNumber, minimum, maximum, multipleOf, exclusiveMinimum, exclusiveMaximum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NumberSchema) {
            NumberSchema that = (NumberSchema) o;
            return that.canEqual(this) &&
                    requiresNumber == that.requiresNumber &&
                    Objects.equals(exclusiveMinimum, that.exclusiveMinimum) &&
                    Objects.equals(exclusiveMaximum, that.exclusiveMaximum) &&
                    Objects.equals(minimum, that.minimum) &&
                    Objects.equals(maximum, that.maximum) &&
                    Objects.equals(multipleOf, that.multipleOf) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NumberSchema;
    }

    @Override
    protected void propertiesToJson(JsonSchemaGenerator writer) {
        writer.writeType(JsonSchemaType.NUMBER, requiresNumber)
                .optionalWrite(JsonSchemaKeyword.MINIMUM, minimum)
                .optionalWrite(JsonSchemaKeyword.MAXIMUM, maximum)
                .optionalWrite(JsonSchemaKeyword.MULTIPLE_OF, multipleOf)
                .optionalWrite(JsonSchemaKeyword.EXCLUSIVE_MINIMUM, exclusiveMinimum)
                .optionalWrite(JsonSchemaKeyword.EXCLUSIVE_MAXIMUM, exclusiveMinimum);
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

        public Builder exclusiveMaximum(final Integer exclusiveMaximum) {
            this.exclusiveMaximum = exclusiveMaximum;
            return this;
        }

        public Builder exclusiveMinimum(final Integer exclusiveMinimum) {
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
