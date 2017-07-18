package io.dugnutt.jsonschema.six;

import javax.validation.constraints.Min;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@code String} schema validator.
 */
public class StringSchema extends Schema {

    @Min(0)
    private final Integer minLength;

    @Min(0)
    private final Integer maxLength;
    private final Pattern pattern;
    private final boolean requiresString;
    private final FormatType formatType;
    private final String format;

    public StringSchema() {
        this(builder());
    }

    /**
     * Constructor.
     *
     * @param builder the builder object containing validation criteria
     */
    public StringSchema(final Builder builder) {
        super(builder);
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.requiresString = builder.requiresString;
        if (builder.pattern != null) {
            this.pattern = Pattern.compile(builder.pattern);
        } else {
            this.pattern = null;
        }
        this.format = builder.format;
        this.formatType = FormatType.fromFormat(builder.format);
    }

    public static Builder builder() {
        return new Builder();
    }

    public FormatType getFormatType() {
        return formatType;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minLength, maxLength, pattern, requiresString, formatType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof StringSchema) {
            StringSchema that = (StringSchema) o;
            return that.canEqual(this) &&
                    requiresString == that.requiresString &&
                    Objects.equals(minLength, that.minLength) &&
                    Objects.equals(maxLength, that.maxLength) &&
                    Objects.equals(patternIfNotNull(pattern), patternIfNotNull(that.pattern)) &&
                    Objects.equals(formatType, that.formatType) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof StringSchema;
    }

    @Override
    protected void writePropertiesToJson(JsonSchemaGenerator writer) {
        writer.writeType(JsonSchemaType.STRING, requiresString)
                .optionalWrite(JsonSchemaKeyword.MIN_LENGTH, minLength)
                .optionalWrite(JsonSchemaKeyword.MAX_LENGTH, maxLength)
                .optionalWrite(pattern)
                .optionalWrite(formatType);

    }

    public boolean requiresString() {
        return requiresString;
    }

    private String patternIfNotNull(Pattern pattern) {
        if (pattern == null) {
            return null;
        } else {
            return pattern.pattern();
        }
    }

    /**
     * Builder class for {@link StringSchema}.
     */
    public static class Builder extends Schema.Builder<StringSchema> {

        public String format;
        private Integer minLength;
        private Integer maxLength;
        private String pattern;
        private boolean requiresString = true;

        // private FormatValidator formatValidator = FormatValidator.NONE;

        @Override
        public StringSchema build() {
            return new StringSchema(this);
        }

        /**
         * Setter for the format validator.
         *
         * @param format the format validator
         * @return {@code this}
         */
        public Builder format(final String format) {
            this.format = checkNotNull(format, "format cannot be null");
            return this;
        }

        public Builder maxLength(final Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder minLength(final Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder pattern(final String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder requiresString(final boolean requiresString) {
            this.requiresString = requiresString;
            return this;
        }
    }
}
