package io.sbsp.jsonschema.validation.keywords.number;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.Builder;

public class NumberExclusiveMaximumValidator extends KeywordValidator<LimitKeyword> {
    private final double exclusiveMaximum;

    @Builder
    public NumberExclusiveMaximumValidator(Schema schema, double exclusiveMaximum) {
        super(Keywords.EXCLUSIVE_MAXIMUM, schema);
        this.exclusiveMaximum = exclusiveMaximum;
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        final double subjectDouble = subject.asJsonNumber().doubleValue();

        if (!(subjectDouble < exclusiveMaximum)) {
            report.addError(buildKeywordFailure(subject)
                    .message("Value is not lower than %s", exclusiveMaximum)
                    .build());
        }

        return report.isValid();
    }
}
