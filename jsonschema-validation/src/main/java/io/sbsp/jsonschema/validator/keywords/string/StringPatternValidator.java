package io.sbsp.jsonschema.validator.keywords.string;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PATTERN;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringPatternValidator extends KeywordValidator<StringKeyword> {

    @NonNull
    private final Pattern pattern;

    @Builder
    public StringPatternValidator(Schema schema, Pattern pattern) {
        super(SchemaKeyword.pattern, schema);
        this.pattern = checkNotNull(pattern);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        String stringSubject = subject.asString();
        if (!patternMatches(pattern, stringSubject)) {
            report.addError(
                    buildKeywordFailure(subject, schema, PATTERN)
                            .message("string [%s] does not match pattern %s", stringSubject, pattern.pattern())
                            .build());
        }
        return report.isValid();
    }

    private boolean patternMatches(Pattern pattern, final String string) {
        return pattern.matcher(string).find();
    }
}
