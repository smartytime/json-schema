package io.dugnutt.jsonschema.six;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.json.JsonString;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.FORMAT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_LENGTH;

/**
 * {@code String} schema validator.
 */
@Builder(builderClassName = "StringKeywordsBuilder")
@Getter
@EqualsAndHashCode
public class StringKeywords implements SchemaKeywords<JsonString> {

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
                .optionalWrite(getPattern())
                .optionalWrite(FORMAT, getFormat());
    }

    static class StringKeywordsBuilder {
    }
}
