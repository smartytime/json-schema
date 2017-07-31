package io.dugnutt.jsonschema.validator.keywords;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;

import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.NOT;
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
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final ValidationReport trap = report.createChildReport();
        if (notValidator.validate(subject, trap)) {
            report.addError(buildKeywordFailure(subject, schema, NOT)
                    .message("subject must not be valid against schema", notSchema.getPointerFragmentURI())
                    .build());
        }
        return report.isValid();
    }
}
