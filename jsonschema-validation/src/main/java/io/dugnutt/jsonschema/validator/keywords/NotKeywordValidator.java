package io.dugnutt.jsonschema.validator.keywords;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NotKeywordValidator extends KeywordValidator {
    private final SchemaValidator notValidator;
    private final Schema notSchema;
    @Builder
    public NotKeywordValidator(Schema schema, SchemaValidator notValidator, Schema notSchema) {
        super(NOT, schema);
        this.notValidator = notValidator;
        this.notSchema = notSchema;
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        final ValidationReport trap = new ValidationReport();
        if (notValidator.validate(subject, trap)) {
            return report.addError(buildKeywordFailure(subject, schema, NOT)
                    .message("subject must not be valid against schema", notSchema.getPointerFragmentURI())
                    .build());
        }
        return true;
    }
}
