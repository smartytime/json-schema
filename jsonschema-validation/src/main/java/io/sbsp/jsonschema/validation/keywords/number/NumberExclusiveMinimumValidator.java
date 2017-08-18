package io.sbsp.jsonschema.validation.keywords.number;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.Builder;

public class NumberExclusiveMinimumValidator extends KeywordValidator<LimitKeyword> {
    private final double exclusiveMinimum;

    @Builder
    public NumberExclusiveMinimumValidator(Schema schema, double exclusiveMinimum) {
        super(Keywords.EXCLUSIVE_MINIMUM, schema);
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        final double subjectDouble = subject.asJsonNumber().doubleValue();

        if (!(subjectDouble > exclusiveMinimum)) {
            report.addError(buildKeywordFailure(subject)
                    .message("Value is not higher than %s", exclusiveMinimum)
                    .build());
        }
        return report.isValid();
    }
}
