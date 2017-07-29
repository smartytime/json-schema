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

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class AllOfValidator extends KeywordValidator {

    @Singular
    @NonNull
    private final List<SchemaValidator> allOfValidators;

    @Builder
    public AllOfValidator(Schema schema, List<SchemaValidator> allOfValidators) {
        super(ALL_OF, schema);
        this.allOfValidators = allOfValidators;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {
        ValidationReport report = parentReport.createChildReport();
        for (SchemaValidator validator : allOfValidators) {
            validator.validate(subject, report);
        }

        List<ValidationError> failures = report.getErrors();

        int matchingCount = allOfValidators.size() - failures.size();
        int subschemaCount = allOfValidators.size();

        if (matchingCount < subschemaCount) {
            parentReport.addError(buildKeywordFailure(subject, schema, ALL_OF)
                    .message("only %d subschema matches out of %d", matchingCount, subschemaCount)
                    .causingExceptions(failures)
                    .build());
        }

        return parentReport.isValid();
    }

}
