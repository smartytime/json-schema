package io.sbsp.jsonschema.validation.keywords.array;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.NonNull;

public class ArrayContainsValidator extends KeywordValidator<SingleSchemaKeyword> {

    @NonNull
    private final SchemaValidator containsValidator;

    public ArrayContainsValidator(SingleSchemaKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.CONTAINS, schema);
        this.containsValidator = factory.createValidator(keyword.getSchema());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        for (int i = 0; i < subject.arraySize(); i++) {
            final ValidationReport trap = report.createChildReport();
            final JsonValueWithPath item = subject.getItem(i);
            if (containsValidator.validate(item, trap)) {
                return true;
            }
        }

        report.addError(buildKeywordFailure(subject)
                .message("array does not contain at least 1 matching item")
                .build());
        return report.isValid();
    }
}
