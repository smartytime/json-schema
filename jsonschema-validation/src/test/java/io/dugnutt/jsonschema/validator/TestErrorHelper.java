package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.enums.JsonSchemaType;

import static com.google.common.base.Preconditions.checkNotNull;

public class TestErrorHelper {
    public static ValidationError failure(Schema schema, JsonSchemaType desired, JsonSchemaType found) {
        checkNotNull(schema, "schema" +
                " must not be null");
        return ValidationError.validationBuilder()
                .schemaLocation(schema.getLocation().getUniqueURI())
                .message(ValidationErrorHelper.TYPE_MISMATCH_ERROR_MESSAGE, desired, found)
                .keyword(JsonSchemaKeyword.TYPE)
                .pointerToViolation(schema.getLocation().getJsonPath())
                .build();
    }

}
