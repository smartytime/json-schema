package io.sbsp.jsonschema.validator.keywords.string;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_LENGTH;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringMinLengthValidator extends KeywordValidator<NumberKeyword> {
    private final int minLength;

    public StringMinLengthValidator(NumberKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.minLength, schema);
        checkArgument(keyword.getDouble() >= 0, "minLength cannot be negative");
        this.minLength = keyword.getInteger();
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        String string = MoreObjects.firstNonNull(subject.asString(), "");
        int actualLength = string.codePointCount(0, string.length());
        if (actualLength < minLength) {
            report.addError(buildKeywordFailure(subject, schema, MIN_LENGTH)
                    .message("expected minLength: %d, actual: %d", minLength, actualLength)
                    .build());
        }
        return report.isValid();
    }
}
