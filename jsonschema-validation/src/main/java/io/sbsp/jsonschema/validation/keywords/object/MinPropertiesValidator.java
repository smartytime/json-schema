package io.sbsp.jsonschema.validation.keywords.object;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import static com.google.common.base.Preconditions.checkArgument;

public class MinPropertiesValidator extends KeywordValidator<NumberKeyword> {
    private final int minProperties;

    public MinPropertiesValidator(NumberKeyword number, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.MIN_PROPERTIES, schema);
        this.minProperties = number.getInteger();
        checkArgument(minProperties >= 0, "minProperties can't be negative");
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        int actualSize = subject.numberOfProperties();
        if (actualSize < minProperties) {
            report.addError(buildKeywordFailure(subject)
                    .message("minimum size: [%d], found: [%d]", minProperties, actualSize)
                    .build());
        }
        return report.isValid();
    }
}
