package io.sbsp.jsonschema.validator.keywords.number;

import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MINIMUM;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberMinimumValidator extends KeywordValidator {
    private final double minimum;

    @Builder
    public NumberMinimumValidator(Schema schema, double minimum) {
        super(JsonSchemaKeyword.MINIMUM, schema);
        this.minimum = minimum;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final double subjectNumber = subject.asJsonNumber().doubleValue();

        if (!(subjectNumber >= minimum)) {
            report.addError(buildKeywordFailure(subject, schema, MINIMUM)
                    .message("Value is not higher or equal to %s", minimum)
                    .build());
        }
        return report.isValid();
    }
}
