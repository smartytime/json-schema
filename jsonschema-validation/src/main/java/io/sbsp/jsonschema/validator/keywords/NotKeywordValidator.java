package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;

import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.NOT;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

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
