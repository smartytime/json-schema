package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;

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
