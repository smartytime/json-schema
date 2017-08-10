package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.NOT;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NotKeywordValidator extends KeywordValidator<SingleSchemaKeyword> {
    private final SchemaValidator notValidator;
    private final Schema notSchema;
    @Builder
    public NotKeywordValidator(Schema schema, SchemaValidator notValidator, Schema notSchema) {
        super(SchemaKeyword.not, schema);
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
