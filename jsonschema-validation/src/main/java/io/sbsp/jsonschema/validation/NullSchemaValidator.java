package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.utils.Schemas;

public class NullSchemaValidator implements SchemaValidator {

    private static final NullSchemaValidator NULL_SCHEMA_VALIDATOR = new NullSchemaValidator();

    private NullSchemaValidator() {
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        if (subject.isNotNull()) {
            report.addError(ValidationErrorHelper.buildTypeMismatchError(subject, Schemas.nullSchema(), JsonSchemaType.NULL)
                    .build());
        }
        return report.isValid();
    }

    @Override
    public Schema getSchema() {
        return Schemas.nullSchema();
    }

    public static NullSchemaValidator getInstance() {
        return NULL_SCHEMA_VALIDATOR;
    }
}
