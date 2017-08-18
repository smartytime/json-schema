package io.sbsp.jsonschema.validation.keywords.object;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;

public class MaxPropertiesValidator extends KeywordValidator<NumberKeyword> {
    private final int maxProperties;

    @Builder
    public MaxPropertiesValidator(NumberKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.MAX_PROPERTIES, schema);
        this.maxProperties = keyword.getInteger();
        checkArgument(maxProperties >= 0, "maxProperties can't be negative");
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        int actualSize = subject.numberOfProperties();
        if (actualSize > maxProperties) {
            report.addError(buildKeywordFailure(subject)
                    .message("maximum size: [%d], found: [%d]", maxProperties, actualSize)
                    .build());
        }
        return report.isValid();
    }
}
