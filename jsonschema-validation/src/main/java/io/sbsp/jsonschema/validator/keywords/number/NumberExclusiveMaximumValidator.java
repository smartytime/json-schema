package io.sbsp.jsonschema.validator.keywords.number;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.EXCLUSIVE_MAXIMUM;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberExclusiveMaximumValidator extends KeywordValidator<LimitKeyword> {
    private final double exclusiveMaximum;

    @Builder
    public NumberExclusiveMaximumValidator(Schema schema, double exclusiveMaximum) {
        super(Keywords.exclusiveMaximum, schema);
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
