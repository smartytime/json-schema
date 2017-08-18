package io.sbsp.jsonschema.validation.keywords.string;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import static com.google.common.base.Preconditions.checkArgument;

public class StringMinLengthValidator extends KeywordValidator<NumberKeyword> {
    private final int minLength;

    public StringMinLengthValidator(NumberKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.MIN_LENGTH, schema);
        checkArgument(keyword.getDouble() >= 0, "minLength cannot be negative");
        this.minLength = keyword.getInteger();
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        String string = MoreObjects.firstNonNull(subject.asString(), "");
        int actualLength = string.codePointCount(0, string.length());
        if (actualLength < minLength) {
            report.addError(buildKeywordFailure(subject)
                    .message("expected minLength: %d, actual: %d", minLength, actualLength)
                    .build());
        }
        return report.isValid();
    }
}
