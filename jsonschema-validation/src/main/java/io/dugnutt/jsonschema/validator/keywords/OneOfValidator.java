package io.dugnutt.jsonschema.validator.keywords;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationError;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class OneOfValidator extends KeywordValidator {

    @Singular
    @NonNull
    private final List<SchemaValidator> oneOfValidators;

    @Builder
    public OneOfValidator(Schema schema, List<SchemaValidator> oneOfValidators) {
        super(ONE_OF, schema);
        this.oneOfValidators = oneOfValidators;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {
        ValidationReport report = parentReport.createChildReport();
        for (SchemaValidator validator : oneOfValidators) {
            validator.validate(subject, report);
        }

        List<ValidationError> failures = report.getErrors();

        int matchingCount = oneOfValidators.size() - failures.size();

        if (matchingCount != 1) {
            parentReport.addError(buildKeywordFailure(subject, schema, ONE_OF)
                    .message("%d subschemas matched instead of one", matchingCount)
                    .causingExceptions(failures)
                    .build());
        }

        return parentReport.isValid();
    }
}
