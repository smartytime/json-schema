package io.sbsp.jsonschema.validator.keywords.object;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_PROPERTIES;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class MaxPropertiesValidator extends KeywordValidator<NumberKeyword> {
    private final int maxProperties;

    @Builder
    public MaxPropertiesValidator(NumberKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.maxProperties, schema);
        this.maxProperties = keyword.getInteger();
        checkArgument(maxProperties >= 0, "maxProperties can't be negative");
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
