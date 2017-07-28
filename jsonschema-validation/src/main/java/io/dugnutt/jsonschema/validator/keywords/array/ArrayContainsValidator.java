package io.dugnutt.jsonschema.validator.keywords.array;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import lombok.Builder;
import lombok.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONTAINS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ITEMS;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayContainsValidator extends KeywordValidator {
    @NonNull
    private final SchemaValidator containsValidator;

    @Builder
    public ArrayContainsValidator(Schema schema, SchemaValidator containsValidator) {
        super(ITEMS, schema);
        this.containsValidator = checkNotNull(containsValidator);
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        final ValidationReport trap = new ValidationReport();
        for (int i = 0; i < subject.arraySize(); i++) {
            final PathAwareJsonValue item = subject.getItem(i);
            if (containsValidator.validate(subject, trap)) {
                return true;
            }
        }

        return report.addError(buildKeywordFailure(subject, schema, CONTAINS)
                .message("array does not contain at least 1 matching item")
                .build());
    }
}
