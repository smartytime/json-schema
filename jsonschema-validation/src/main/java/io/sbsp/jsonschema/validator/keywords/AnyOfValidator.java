package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ANY_OF;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

@EqualsAndHashCode(callSuper = true)
@ToString
public class AnyOfValidator extends KeywordValidator {

    @Singular
    @NonNull
    private final List<SchemaValidator> anyOfValidators;

    @Builder
    public AnyOfValidator(Schema schema, List<SchemaValidator> anyOfValidators) {
        super(ANY_OF, schema);
        this.anyOfValidators = anyOfValidators;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {
        ValidationReport anyOfReport = parentReport.createChildReport();
        for (SchemaValidator anyOfValidator : anyOfValidators) {
            ValidationReport trap = anyOfReport.createChildReport();
            if (anyOfValidator.validate(subject, trap)) {
                return true;
            }
            anyOfReport.addReport(schema, subject, trap);
        }

        parentReport.addError(buildKeywordFailure(subject, schema, ANY_OF)
                .message("no subschema matched out of the total %d subschemas", anyOfValidators.size())
                .causingExceptions(anyOfReport.getErrors())
                .build());
        return parentReport.isValid();
    }
}
