package io.dugnutt.jsonschema.validator.keywords.object;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.MIN_PROPERTIES;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class MinPropertiesValidator extends KeywordValidator {
    private final int minProperties;

    @Builder
    public MinPropertiesValidator(Schema schema, int minProperties) {
        super(MIN_PROPERTIES, schema);
        checkArgument(minProperties >= 0, "minProperties can't be negative");

        this.minProperties = minProperties;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        int actualSize = subject.numberOfProperties();
        if (actualSize < minProperties) {
            report.addError(buildKeywordFailure(subject, schema, MIN_PROPERTIES)
                    .message("minimum size: [%d], found: [%d]", minProperties, actualSize)
                    .build());
        }
        return report.isValid();
    }
}
