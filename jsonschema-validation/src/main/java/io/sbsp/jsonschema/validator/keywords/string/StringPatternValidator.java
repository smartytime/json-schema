package io.sbsp.jsonschema.validator.keywords.string;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import java.util.regex.Pattern;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PATTERN;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringPatternValidator extends KeywordValidator<StringKeyword> {

    private final Pattern pattern;

    public StringPatternValidator(StringKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.pattern, schema);
        this.pattern = Pattern.compile(keyword.getKeywordValue());
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
