package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.utils.Schemas;
import lombok.Getter;

@Getter
public class FalseSchemaValidator implements SchemaValidator {

    private static final FalseSchemaValidator FALSE_SCHEMA_VALIDATOR = new FalseSchemaValidator();

    private FalseSchemaValidator() {
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        report.addError(ValidationErrorHelper.buildKeywordFailure(subject, Schemas.nullSchema(), null)
                .message("no value allowed, found [%s]", subject.getWrapped())
                .build());
        return report.isValid();
    }

    @Override
    public Schema getSchema() {
        return Schemas.falseSchema();
    }

    public static FalseSchemaValidator getInstance() {
        return FALSE_SCHEMA_VALIDATOR;
    }
}
