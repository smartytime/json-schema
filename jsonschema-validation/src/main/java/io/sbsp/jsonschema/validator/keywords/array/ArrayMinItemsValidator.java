package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_ITEMS;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayMinItemsValidator extends KeywordValidator {
    private final int minItems;

    @Builder
    public ArrayMinItemsValidator(Schema schema, int minItems) {
        super(MIN_ITEMS, schema);
        checkArgument(minItems >= 0, "minItems can't be negative");

        this.minItems = minItems;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        int actualLength = subject.arraySize();

        if (actualLength < minItems) {
            report.addError(buildKeywordFailure(subject, schema, MIN_ITEMS)
                    .message("expected minimum item count: %s, found: %s", minItems, actualLength)
                    .build());
        }
        return report.isValid();
    }
}
