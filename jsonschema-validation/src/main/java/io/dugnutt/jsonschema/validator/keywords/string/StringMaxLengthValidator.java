package io.dugnutt.jsonschema.validator.keywords.string;

import com.google.common.base.MoreObjects;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_LENGTH;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringMaxLengthValidator extends KeywordValidator {
    private final int maxLength;

    @Builder
    public StringMaxLengthValidator(Schema schema, int maxLength) {
        super(JsonSchemaKeyword.MAX_LENGTH, schema);
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
