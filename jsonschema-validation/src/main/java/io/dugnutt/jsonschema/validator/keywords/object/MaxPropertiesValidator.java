package io.dugnutt.jsonschema.validator.keywords.object;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.MAX_PROPERTIES;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class MaxPropertiesValidator extends KeywordValidator {
    private final int maxProperties;

    @Builder
    public MaxPropertiesValidator(Schema schema, int maxProperties) {
        super(MAX_PROPERTIES, schema);
        checkArgument(maxProperties >= 0, "maxProperties can't be negative");

        this.maxProperties = maxProperties;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        int actualSize = subject.numberOfProperties();
        if (actualSize > maxProperties) {
            report.addError(buildKeywordFailure(subject, schema, MAX_PROPERTIES)
                    .message("maximum size: [%d], found: [%d]", maxProperties, actualSize)
                    .build());
        }
        return report.isValid();
    }
}
