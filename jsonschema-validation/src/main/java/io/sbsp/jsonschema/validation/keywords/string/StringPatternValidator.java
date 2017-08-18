package io.sbsp.jsonschema.validation.keywords.string;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import java.util.regex.Pattern;

public class StringPatternValidator extends KeywordValidator<StringKeyword> {

    private final Pattern pattern;

    public StringPatternValidator(StringKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.PATTERN, schema);
        this.pattern = Pattern.compile(keyword.getKeywordValue());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        String stringSubject = subject.asString();
        if (!patternMatches(pattern, stringSubject)) {
            report.addError(
                    buildKeywordFailure(subject)
                            .message("string [%s] does not match pattern %s", stringSubject, pattern.pattern())
                            .build());
        }
        return report.isValid();
    }

    private boolean patternMatches(Pattern pattern, final String string) {
        return pattern.matcher(string).find();
    }
}
