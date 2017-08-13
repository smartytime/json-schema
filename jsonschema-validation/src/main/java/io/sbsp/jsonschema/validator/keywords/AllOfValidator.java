package io.sbsp.jsonschema.validator.keywords;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationError;
import io.sbsp.jsonschema.validator.ValidationReport;

import java.util.List;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ALL_OF;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class AllOfValidator extends KeywordValidator<SchemaListKeyword> {

    private final List<SchemaValidator> allOfValidators;

    public AllOfValidator(SchemaListKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.allOf, schema);
        this.allOfValidators = keyword.getSchemas().stream()
                .map(factory::createValidator)
                .collect(ImmutableList.toImmutableList());
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
