package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.validator.ValidationError;
import io.dugnutt.jsonschema.validator.ValidationErrorHelper;

import static com.google.common.base.Preconditions.checkNotNull;

public class TestErrorHelper {
    public static ValidationError failure(JsonSchema schema, JsonSchemaType desired, JsonSchemaType found) {
        checkNotNull(schema, "schema must not be null");
        return ValidationError.validationBuilder()
                .schemaLocation(schema.getLocation().getAbsoluteURI())
                .message(ValidationErrorHelper.TYPE_MISMATCH_ERROR_MESSAGE, desired, found)
                .keyword(JsonSchemaKeyword.TYPE)
                .pointerToViolation(schema.getLocation().getJsonPath())
                .build();
    }

}
