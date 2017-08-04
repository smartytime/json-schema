package io.sbsp.jsonschema.validator.keywords.number;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAXIMUM;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberMaximumValidator extends KeywordValidator {
    private final double maximum;

    @Builder
    public NumberMaximumValidator(Schema schema, double maximum) {
        super(JsonSchemaKeywordType.MAXIMUM, schema);
        this.maximum = maximum;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final double subjectNumber = subject.asJsonNumber().doubleValue();

        // max = 10
        // sub = 10
        // this is okay
        if (!(subjectNumber <= maximum)) {
            report.addError(buildKeywordFailure(subject, schema, MAXIMUM)
                    .message("Value not lower or equal to %s", maximum)
                    .build());
        }
        return report.isValid();
    }
}
