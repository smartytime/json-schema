package io.dugnutt.jsonschema.validator.keywords.string;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.PATTERN;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringPatternValidator extends KeywordValidator {

    @NonNull
    private final Pattern pattern;

    @Builder
    public StringPatternValidator(Schema schema, Pattern pattern) {
        super(PATTERN, schema);
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
