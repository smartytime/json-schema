package io.dugnutt.jsonschema.validator.keywords.array;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import static com.google.common.base.Preconditions.checkArgument;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_ITEMS;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayMaxItemsValidator extends KeywordValidator {
    private final int maxItems;

    @Builder
    public ArrayMaxItemsValidator(Schema schema, int maxItems) {
        super(MAX_ITEMS, schema);
        checkArgument(maxItems >= 0, "maxItems can't be negative");

        this.maxItems = maxItems;
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        int actualLength = subject.arraySize();

        if (actualLength > maxItems) {
            return report.addError(buildKeywordFailure(subject, schema, MAX_ITEMS)
                    .message("expected maximum item count: %s, found: %s", maxItems, actualLength)
                    .build());
        }
        return true;
    }
}
