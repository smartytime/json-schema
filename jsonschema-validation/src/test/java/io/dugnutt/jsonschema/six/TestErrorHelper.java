package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationError;

import static com.google.common.base.Preconditions.checkNotNull;

public class TestErrorHelper {
    public static ValidationError failure(Schema schema, JsonSchemaType desired, JsonSchemaType found) {
        checkNotNull(schema, "schema must not be null");
        return ValidationError.validationBuilder()
                .schemaLocation(schema.getLocation().getAbsoluteURI())
                .message(SchemaValidator.TYPE_MISMATCH_ERROR_MESSAGE, desired, found)
                .keyword(JsonSchemaKeyword.TYPE)
                .pointerToViolation(schema.getLocation().getJsonPath())
                .build();
    }

}
