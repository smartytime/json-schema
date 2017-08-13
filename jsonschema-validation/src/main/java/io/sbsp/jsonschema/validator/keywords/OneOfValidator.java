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

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ONE_OF;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class OneOfValidator extends KeywordValidator<SchemaListKeyword> {

    private final List<SchemaValidator> oneOfValidators;

    public OneOfValidator(SchemaListKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.oneOf, schema);
        this.oneOfValidators = keyword.getSchemas().stream()
                .map(factory::createValidator)
                .collect(ImmutableList.toImmutableList());
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
