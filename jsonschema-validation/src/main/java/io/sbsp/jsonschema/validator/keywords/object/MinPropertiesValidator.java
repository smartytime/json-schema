package io.sbsp.jsonschema.validator.keywords.object;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_PROPERTIES;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class MinPropertiesValidator extends KeywordValidator<NumberKeyword> {
    private final int minProperties;

    public MinPropertiesValidator(NumberKeyword number, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.minProperties, schema);
        this.minProperties = number.getInteger();
        checkArgument(minProperties >= 0, "minProperties can't be negative");
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
