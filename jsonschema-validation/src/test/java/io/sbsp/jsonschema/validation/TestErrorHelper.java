package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.Keywords;

import static com.google.common.base.Preconditions.checkNotNull;

public class TestErrorHelper {
    public static ValidationError failure(Schema schema, JsonSchemaType desired, JsonSchemaType found) {
        checkNotNull(schema, "schema" +
                " must not be null");
        return ValidationError.validationBuilder()
                .violatedSchema(schema)
                .message(ValidationErrorHelper.TYPE_MISMATCH_ERROR_MESSAGE, desired, found)
                .keyword(Keywords.TYPE)
                .pointerToViolation(schema.getLocation().getJsonPath())
                .build();
    }

}
