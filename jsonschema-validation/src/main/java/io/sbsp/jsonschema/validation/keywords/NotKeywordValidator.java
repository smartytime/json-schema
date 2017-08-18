package io.sbsp.jsonschema.validation.keywords;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;

public class NotKeywordValidator extends KeywordValidator<SingleSchemaKeyword> {
    private final SchemaValidator notValidator;
    private final Schema notSchema;

    public NotKeywordValidator(SingleSchemaKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.NOT, schema);
        this.notValidator = factory.createValidator(keyword.getSchema());
        this.notSchema = schema;
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        final ValidationReport trap = report.createChildReport();
        if (notValidator.validate(subject, trap)) {
            report.addError(buildKeywordFailure(subject)
                    .message("subject must not be valid against schema", notSchema.getPointerFragmentURI())
                    .build());
        }
        return report.isValid();
    }
}
