package io.sbsp.jsonschema.validator.keywords.number;

import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.EXCLUSIVE_MAXIMUM;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberExclusiveMaximumValidator extends KeywordValidator {
    private final double exclusiveMaximum;

    @Builder
    public NumberExclusiveMaximumValidator(Schema schema, double exclusiveMaximum) {
        super(JsonSchemaKeyword.EXCLUSIVE_MAXIMUM, schema);
        this.exclusiveMaximum = exclusiveMaximum;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final double subjectDouble = subject.asJsonNumber().doubleValue();

        if (!(subjectDouble < exclusiveMaximum)) {
            report.addError(buildKeywordFailure(subject, schema, EXCLUSIVE_MAXIMUM)
                    .message("Value is not lower than %s", exclusiveMaximum)
                    .build());
        }

        return report.isValid();
    }
}
