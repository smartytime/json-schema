package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.NonNull;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONTAINS;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayContainsValidator extends KeywordValidator<SingleSchemaKeyword> {

    @NonNull
    private final SchemaValidator containsValidator;

    public ArrayContainsValidator(SingleSchemaKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.contains, schema);
        this.containsValidator = factory.createValidator(keyword.getSchema());
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        for (int i = 0; i < subject.arraySize(); i++) {
            final ValidationReport trap = report.createChildReport();
            final JsonValueWithLocation item = subject.getItem(i);
            if (containsValidator.validate(item, trap)) {
                return true;
            }
        }

        report.addError(buildKeywordFailure(subject, schema, CONTAINS)
                .message("array does not contain at least 1 matching item")
                .build());
        return report.isValid();
    }
}
