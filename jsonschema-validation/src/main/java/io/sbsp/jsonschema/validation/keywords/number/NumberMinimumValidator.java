package io.sbsp.jsonschema.validation.keywords.number;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.Builder;

public class NumberMinimumValidator extends KeywordValidator<LimitKeyword> {
    private final double minimum;

    @Builder
    public NumberMinimumValidator(Schema schema, double minimum) {
        super(Keywords.MINIMUM, schema);
        this.minimum = minimum;
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        final double subjectNumber = subject.asJsonNumber().doubleValue();

        if (!(subjectNumber >= minimum)) {
            report.addError(buildKeywordFailure(subject)
                    .message("Value is not higher or equal to %s", minimum)
                    .build());
        }
        return report.isValid();
    }
}
