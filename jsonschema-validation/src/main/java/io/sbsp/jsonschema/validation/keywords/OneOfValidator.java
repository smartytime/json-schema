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

public class OneOfValidator extends KeywordValidator<SchemaListKeyword> {

    private final List<SchemaValidator> oneOfValidators;

    public OneOfValidator(SchemaListKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.ONE_OF, schema);
        this.oneOfValidators = keyword.getSchemas().stream()
                .map(factory::createValidator)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport parentReport) {
        ValidationReport report = parentReport.createChildReport();
        for (SchemaValidator validator : oneOfValidators) {
            validator.validate(subject, report);
        }

        List<ValidationError> failures = report.getErrors();

        int matchingCount = oneOfValidators.size() - failures.size();

        if (matchingCount != 1) {
            parentReport.addError(buildKeywordFailure(subject)
                    .message("%d subschemas matched instead of one", matchingCount)
                    .causingExceptions(failures)
                    .build());
        }

        return parentReport.isValid();
    }
}
