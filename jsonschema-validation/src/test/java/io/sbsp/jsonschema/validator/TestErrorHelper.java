package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.enums.JsonSchemaType;

import static com.google.common.base.Preconditions.checkNotNull;

public class TestErrorHelper {
    public static ValidationError failure(Schema schema, JsonSchemaType desired, JsonSchemaType found) {
        checkNotNull(schema, "schema" +
                " must not be null");
        return ValidationError.validationBuilder()
                .violatedSchema(schema)
                .message(ValidationErrorHelper.TYPE_MISMATCH_ERROR_MESSAGE, desired, found)
                .keyword(JsonSchemaKeywordType.TYPE)
                .pointerToViolation(schema.getLocation().getJsonPath())
                .build();
    }

}
