package io.sbsp.jsonschema;

import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;

public class ValidationMocks {
    public static SchemaValidator createTestValidator(JsonSchemaBuilder schema) {
        return createTestValidator(schema.build());
    }

    public static SchemaValidator createTestValidator(Schema schema) {
        return SchemaValidatorFactory.createValidatorForSchema(schema);
    }
}
