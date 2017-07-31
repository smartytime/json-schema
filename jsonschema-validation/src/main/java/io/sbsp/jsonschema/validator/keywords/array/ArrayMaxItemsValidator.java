package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MAX_ITEMS;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayMaxItemsValidator extends KeywordValidator {
    private final int maxItems;

    @Builder
    public ArrayMaxItemsValidator(Schema schema, int maxItems) {
        super(MAX_ITEMS, schema);
        checkArgument(maxItems >= 0, "maxItems can't be negative");

        this.maxItems = maxItems;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        int actualLength = subject.arraySize();

        if (actualLength > maxItems) {
            report.addError(buildKeywordFailure(subject, schema, MAX_ITEMS)
                    .message("expected maximum item count: %s, found: %s", maxItems, actualLength)
                    .build());
        }
        return report.isValid();
    }
}
