package io.sbsp.jsonschema.keywords;

import com.google.common.base.Objects;
import io.sbsp.jsonschema.JsonSchemaGenerator;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.FORMAT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_LENGTH;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_LENGTH;

/**
 * {@code String} schema validator.
 */
@Builder
@Getter
public class StringKeywords implements SchemaKeywords {

    @Min(0)
    @Nullable
    private final Integer minLength;

    @Min(0)
    @Nullable
    private final Integer maxLength;

    @Nullable
    private final Pattern pattern;

    @Nullable
    private final String format;

    @Override
    public Set<JsonSchemaType> getApplicableTypes() {
        return Collections.singleton(JsonSchemaType.STRING);
    }

    @Override
    public JsonSchemaGenerator toJson(JsonSchemaGenerator writer) {
        return writer
                .optionalWrite(MIN_LENGTH, getMinLength())
                .optionalWrite(MAX_LENGTH, getMaxLength())
                .optionalWrite(pattern)
                .optionalWrite(FORMAT, getFormat());
    }

    public Pattern getPattern() {
        if (pattern == null) {
            throw new NullPointerException("Pattern is null.  Use findPattern to get an Optional instead");
        }
        return pattern;
    }

    private String getPatternString() {
        return pattern != null ? pattern.pattern() : null;
    }


    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof StringKeywords)) {
            return false;
        }
        final StringKeywords that = (StringKeywords) o;
        return Objects.equal(minLength, that.minLength) &&
                Objects.equal(maxLength, that.maxLength) &&
                Objects.equal(getPatternString(), that.getPatternString()) &&
                Objects.equal(format, that.format);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(minLength, maxLength, getPatternString(), format);
    }

    public Optional<Pattern> findPattern() {
        return Optional.ofNullable(pattern);
    }

    public static class StringKeywordsBuilder {
        private String getPatternString() {
            return pattern != null ? pattern.pattern() : null;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof StringKeywordsBuilder)) {
                return false;
            }
            final StringKeywordsBuilder that = (StringKeywordsBuilder) o;
            return Objects.equal(minLength, that.minLength) &&
                    Objects.equal(maxLength, that.maxLength) &&
                    Objects.equal(getPatternString(), that.getPatternString()) &&
                    Objects.equal(format, that.format);
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(minLength, maxLength, getPatternString(), format);
        }
    }

    private static final StringKeywords BLANK_STRING_KEYWORDS = builder().build();

    public static final StringKeywords blankStringKeywords() {
        return BLANK_STRING_KEYWORDS;
    }
}
