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

public class StringMaxLengthValidator extends KeywordValidator<NumberKeyword> {
    private final int maxLength;

    public StringMaxLengthValidator(NumberKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.MAX_LENGTH, schema);
        checkArgument(keyword.getDouble() >= 0, "maxLength cannot be negative");
        this.maxLength = keyword.getInteger();
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        String string = MoreObjects.firstNonNull(subject.asString(), "");
        int actualLength = string.codePointCount(0, string.length());
        if (actualLength > maxLength) {
            report.addError(buildKeywordFailure(subject)
                    .message("expected maxLength: %d, actual: %d", maxLength, actualLength)
                    .build());
        }
        return report.isValid();
    }
}
