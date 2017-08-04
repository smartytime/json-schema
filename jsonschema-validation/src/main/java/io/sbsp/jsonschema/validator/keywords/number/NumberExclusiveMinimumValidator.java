package io.sbsp.jsonschema.validator.keywords.number;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.EXCLUSIVE_MINIMUM;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberExclusiveMinimumValidator extends KeywordValidator {
    private final double exclusiveMinimum;

    @Builder
    public NumberExclusiveMinimumValidator(Schema schema, double exclusiveMinimum) {
        super(JsonSchemaKeywordType.EXCLUSIVE_MINIMUM, schema);
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final double subjectDouble = subject.asJsonNumber().doubleValue();

        if (!(subjectDouble > exclusiveMinimum)) {
            report.addError(buildKeywordFailure(subject, schema, EXCLUSIVE_MINIMUM)
                    .message("Value is not higher than %s", exclusiveMinimum)
                    .build());
        }
        return report.isValid();
    }
}
