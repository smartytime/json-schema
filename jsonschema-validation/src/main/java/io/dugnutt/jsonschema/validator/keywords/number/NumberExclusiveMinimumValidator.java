package io.dugnutt.jsonschema.validator.keywords.number;

import io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberExclusiveMinimumValidator extends KeywordValidator {
    private final double exclusiveMinimum;

    @Builder
    public NumberExclusiveMinimumValidator(Schema schema, double exclusiveMinimum) {
        super(JsonSchemaKeyword.EXCLUSIVE_MINIMUM, schema);
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
