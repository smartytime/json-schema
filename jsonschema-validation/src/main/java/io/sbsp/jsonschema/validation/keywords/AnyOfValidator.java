package io.sbsp.jsonschema.validation.keywords;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString
public class AnyOfValidator extends KeywordValidator<SchemaListKeyword> {

    private final List<SchemaValidator> anyOfValidators;

    public AnyOfValidator(SchemaListKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.ANY_OF, schema);
        this.anyOfValidators = keyword.getSchemas().stream()
                .map(factory::createValidator)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport parentReport) {
        ValidationReport anyOfReport = parentReport.createChildReport();
        for (SchemaValidator anyOfValidator : anyOfValidators) {
            ValidationReport trap = anyOfReport.createChildReport();
            if (anyOfValidator.validate(subject, trap)) {
                return true;
            }
            anyOfReport.addReport(schema, subject, trap);
        }

        parentReport.addError(buildKeywordFailure(subject)
                .message("no subschema matched out of the total %d subschemas", anyOfValidators.size())
                .causingExceptions(anyOfReport.getErrors())
                .build());
        return parentReport.isValid();
    }
}
