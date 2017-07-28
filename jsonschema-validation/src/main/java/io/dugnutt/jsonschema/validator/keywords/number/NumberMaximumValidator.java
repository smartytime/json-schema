package io.dugnutt.jsonschema.validator.keywords.number;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAXIMUM;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberMaximumValidator extends KeywordValidator {
    private final double maximum;

    @Builder
    public NumberMaximumValidator(Schema schema, double maximum) {
        super(JsonSchemaKeyword.MAXIMUM, schema);
        this.maximum = maximum;
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        final double subjectNumber = subject.asJsonNumber().doubleValue();

        // max = 10
        // sub = 10
        // this is okay
        if (!(subjectNumber <= maximum)) {
            return report.addError(buildKeywordFailure(subject, schema, MAXIMUM)
                    .message("Value not lower or equal to %s", maximum)
                    .build());
        }
        return true;
    }
}
