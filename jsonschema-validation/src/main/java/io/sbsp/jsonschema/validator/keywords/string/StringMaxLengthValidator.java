package io.sbsp.jsonschema.validator.keywords.string;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_LENGTH;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringMaxLengthValidator extends KeywordValidator<NumberKeyword> {
    private final int maxLength;

    @Builder
    public StringMaxLengthValidator(Schema schema, int maxLength) {
        super(SchemaKeyword.maxLength, schema);
        checkArgument(maxLength >= 0, "maxLength cannot be negative");
        this.maxLength = maxLength;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        String string = MoreObjects.firstNonNull(subject.asString(), "");
        int actualLength = string.codePointCount(0, string.length());
        if (actualLength > maxLength) {
            report.addError(buildKeywordFailure(subject, schema, MAX_LENGTH)
                    .message("expected maxLength: %d, actual: %d", maxLength, actualLength)
                    .build());
        }
        return report.isValid();
    }
}
