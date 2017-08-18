package io.sbsp.jsonschema.validation.keywords;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationError;
import io.sbsp.jsonschema.validation.ValidationReport;

import java.util.List;

public class AllOfValidator extends KeywordValidator<SchemaListKeyword> {

    private final List<SchemaValidator> allOfValidators;

    public AllOfValidator(SchemaListKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.ALL_OF, schema);
        this.allOfValidators = keyword.getSchemas().stream()
                .map(factory::createValidator)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport parentReport) {
        ValidationReport report = parentReport.createChildReport();
        for (SchemaValidator validator : allOfValidators) {
            validator.validate(subject, report);
        }

        List<ValidationError> failures = report.getErrors();

        int matchingCount = allOfValidators.size() - failures.size();
        int subschemaCount = allOfValidators.size();

        if (matchingCount < subschemaCount) {
            parentReport.addError(this.buildKeywordFailure(subject)
                    .message("only %d subschema matches out of %d", matchingCount, subschemaCount)
                    .causingExceptions(failures)
                    .build());
        }

        return parentReport.isValid();
    }

}
