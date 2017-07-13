package org.everit.jsonschema.api;

import java.util.Objects;

/**
 * Number schema validator.
 */
public class NumberSchema extends Schema {

    private final boolean requiresNumber;
    private final Number minimum;
    private final Number maximum;
    private final Number multipleOf;
    private final boolean exclusiveMinimum;
    private final boolean exclusiveMaximum;
    private final boolean requiresInteger;

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
        this.requiresInteger = builder.requiresInteger;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean requiresNumber() {
        return requiresNumber;
    }

    public Number getMaximum() {
        return maximum;
    }

    public Number getMinimum() {
        return minimum;
    }

    public Number getMultipleOf() {
        return multipleOf;
    }

    public boolean isExclusiveMaximum() {
        return exclusiveMaximum;
    }

    public boolean isExclusiveMinimum() {
        return exclusiveMinimum;
    }

    public boolean requiresInteger() {
        return requiresInteger;
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
                    requiresInteger == that.requiresInteger &&
                    Objects.equals(minimum, that.minimum) &&
                    Objects.equals(maximum, that.maximum) &&
                    Objects.equals(multipleOf, that.multipleOf) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    void describePropertiesTo(JsonWriter writer) {
        if (requiresInteger) {
            writer.key("type").value("integer");
        } else if (requiresNumber) {
            writer.key("type").value("number");
        }
        writer.ifPresent("minimum", minimum);
        writer.ifPresent("maximum", maximum);
        writer.ifPresent("multipleOf", multipleOf);
        writer.ifTrue("exclusiveMinimum", exclusiveMinimum);
        writer.ifTrue("exclusiveMaximum", exclusiveMaximum);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(super.hashCode(), requiresNumber, minimum, maximum, multipleOf, exclusiveMinimum, exclusiveMaximum, requiresInteger);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NumberSchema;
    }

    /**
     * Builder class for {@link NumberSchema}.
     */
    public static class Builder extends Schema.Builder<NumberSchema> {

        private Number minimum;

        private Number maximum;

        private Number multipleOf;

        private boolean exclusiveMinimum = false;

        private boolean exclusiveMaximum = false;

        private boolean requiresNumber = true;

        private boolean requiresInteger = false;

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

        public Builder requiresInteger(final boolean requiresInteger) {
            this.requiresInteger = requiresInteger;
            return this;
        }

        public Builder requiresNumber(final boolean requiresNumber) {
            this.requiresNumber = requiresNumber;
            return this;
        }

    }
}
