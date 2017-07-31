package io.dugnutt.jsonschema.six.keywords;

import io.dugnutt.jsonschema.six.JsonSchemaGenerator;
import io.dugnutt.jsonschema.six.enums.JsonSchemaType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.FORMAT;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.MAX_LENGTH;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.MIN_LENGTH;

/**
 * {@code String} schema validator.
 */
@Builder(builderClassName = "StringKeywordsBuilder")
@Getter
@EqualsAndHashCode(doNotUseGetters = true)
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

    public Optional<Pattern> findPattern() {
        return Optional.ofNullable(pattern);
    }

    public static class StringKeywordsBuilder {
    }
}
