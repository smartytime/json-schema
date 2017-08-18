package io.sbsp.jsonschema.validation.keywords.array;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import static com.google.common.base.Preconditions.checkArgument;

public class ArrayMinItemsValidator extends KeywordValidator<NumberKeyword> {
    private final int minItems;

    public ArrayMinItemsValidator(NumberKeyword number, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.MIN_ITEMS, schema);
        this.minItems = number.getInteger();
        checkArgument(minItems >= 0, "minItems can't be negative");
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        int actualLength = subject.arraySize();

        if (actualLength < minItems) {
            report.addError(buildKeywordFailure(subject)
                    .message("expected minimum item count: %s, found: %s", minItems, actualLength)
                    .build());
        }
        return report.isValid();
    }
}
