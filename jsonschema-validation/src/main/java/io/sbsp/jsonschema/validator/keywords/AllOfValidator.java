package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationError;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ALL_OF;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

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
