package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.CONTAINS;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayContainsValidator extends KeywordValidator {
    @NonNull
    private final SchemaValidator containsValidator;

    @Builder
    public ArrayContainsValidator(Schema schema, SchemaValidator containsValidator) {
        super(CONTAINS, schema);
        this.containsValidator = checkNotNull(containsValidator);
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
