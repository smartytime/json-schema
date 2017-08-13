package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import static com.google.common.base.Preconditions.checkArgument;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_ITEMS;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayMaxItemsValidator extends KeywordValidator<NumberKeyword> {
    private final int maxItems;

    public ArrayMaxItemsValidator(NumberKeyword number, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.maxItems, schema);
        this.maxItems = number.getInteger();
        checkArgument(maxItems >= 0, "maxItems can't be negative");
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
