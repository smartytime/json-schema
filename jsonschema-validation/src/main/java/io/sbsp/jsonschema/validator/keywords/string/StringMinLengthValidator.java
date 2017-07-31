package io.sbsp.jsonschema.validator.keywords.string;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MIN_LENGTH;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringMinLengthValidator extends KeywordValidator {
    private final int minLength;

    @Builder
    public StringMinLengthValidator(Schema schema, int minLength) {
        super(JsonSchemaKeyword.MIN_LENGTH, schema);
        checkArgument(minLength >= 0, "minLength cannot be negative");
        this.minLength = minLength;
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
