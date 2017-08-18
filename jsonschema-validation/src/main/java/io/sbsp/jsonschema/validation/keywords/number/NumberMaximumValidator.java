package io.sbsp.jsonschema.validation.keywords.number;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.Builder;

public class NumberMaximumValidator extends KeywordValidator<LimitKeyword> {
    private final double maximum;

    @Builder
    public NumberMaximumValidator(Schema schema, double maximum) {
        super(Keywords.MAXIMUM, schema);
        this.maximum = maximum;
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        final double subjectNumber = subject.asJsonNumber().doubleValue();

        // max = 10
        // sub = 10
        // this is okay
        if (!(subjectNumber <= maximum)) {
            report.addError(buildKeywordFailure(subject)
                    .message("Value not lower or equal to %s", maximum)
                    .build());
        }
        return report.isValid();
    }
}
