package io.dugnutt.jsonschema.validator.keywords.array;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_ITEMS;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

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
