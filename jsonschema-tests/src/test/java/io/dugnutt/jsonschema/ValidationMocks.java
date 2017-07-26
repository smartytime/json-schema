package io.dugnutt.jsonschema;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import io.dugnutt.jsonschema.validator.JsonSchemaValidator;

import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.DEFAULT_VALIDATOR;

public class ValidationMocks {
    public static JsonSchemaValidator createTestValidator(JsonSchemaBuilder schema) {
        return createTestValidator(schema.build());
    }

    public static JsonSchemaValidator createTestValidator(Schema schema) {
        return DEFAULT_VALIDATOR.createValidator(schema);
    }
}
