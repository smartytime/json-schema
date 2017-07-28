package io.dugnutt.jsonschema.validator.keywords;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

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
    public boolean validate(PathAwareJsonValue subject, ValidationReport parentReport) {
        ValidationReport report = new ValidationReport();
        for (SchemaValidator anyOfValidator : anyOfValidators) {
            if (anyOfValidator.validate(subject, report)) {
                return true;
            }
        }
        return parentReport.addError(buildKeywordFailure(subject, schema, ANY_OF)
                .message("no subschema matched out of the total %d subschemas", anyOfValidators.size())
                .causingExceptions(report.getErrors())
                .build());
    }
}
