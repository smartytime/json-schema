package io.dugnutt.jsonschema.validator.keywords.number;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberExclusiveMinimumValidator extends KeywordValidator {
    private final double exclusiveMinimum;

    @Builder
    public NumberExclusiveMinimumValidator(Schema schema, double exclusiveMinimum) {
        super(JsonSchemaKeyword.EXCLUSIVE_MINIMUM, schema);
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        final double subjectDouble = subject.asJsonNumber().doubleValue();

        if (!(subjectDouble > exclusiveMinimum)) {
            return report.addError(buildKeywordFailure(subject, schema, EXCLUSIVE_MINIMUM)
                    .message("Value is not higher than %s", exclusiveMinimum)
                    .build());
        }
        return true;
    }
}
