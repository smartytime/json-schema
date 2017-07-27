package io.dugnutt.jsonschema;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import io.dugnutt.jsonschema.validator.SchemaValidator;

import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.DEFAULT_VALIDATOR_FACTORY;

public class ValidationMocks {
    public static SchemaValidator createTestValidator(JsonSchemaBuilder schema) {
        return createTestValidator(schema.build());
    }

    public static SchemaValidator createTestValidator(Schema schema) {
        return DEFAULT_VALIDATOR_FACTORY.createValidator(schema);
    }
}
