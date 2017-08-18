package io.sbsp.jsonschema.validation.keywords.array;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import static com.google.common.base.Preconditions.checkArgument;

public class ArrayMaxItemsValidator extends KeywordValidator<NumberKeyword> {
    private final int maxItems;

    public ArrayMaxItemsValidator(NumberKeyword number, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.MAX_ITEMS, schema);
        this.maxItems = number.getInteger();
        checkArgument(maxItems >= 0, "maxItems can't be negative");
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        int actualLength = subject.arraySize();

        if (actualLength > maxItems) {
            report.addError(buildKeywordFailure(subject)
                    .message("expected maximum item count: %s, found: %s", maxItems, actualLength)
                    .build());
        }
        return report.isValid();
    }
}
