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

import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ONE_OF;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

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