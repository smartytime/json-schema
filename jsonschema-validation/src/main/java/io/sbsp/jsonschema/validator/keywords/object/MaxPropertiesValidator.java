package io.sbsp.jsonschema.validator.keywords.object;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MAX_PROPERTIES;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

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
