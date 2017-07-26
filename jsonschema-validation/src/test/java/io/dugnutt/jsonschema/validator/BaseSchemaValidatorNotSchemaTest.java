package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.Schema;
import org.junit.Test;

import javax.json.JsonValue;

import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.validator.ValidationMocks.createTestValidator;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockBooleanSchema;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.expectSuccess;

public class BaseSchemaValidatorNotSchemaTest {
    @Test
    public void failure() {
        Schema subject = jsonSchemaBuilder().notSchema(mockBooleanSchema()).build();
        ValidationTestSupport.failureOf(subject)
                .validator(createTestValidator(subject))
                .input(JsonValue.TRUE)
                .expectedKeyword("not")
                .expect();
    }

    @Test
    public void success() {
        final Schema schemaWithNot = jsonSchemaBuilder().notSchema(mockBooleanSchema()).build();
        expectSuccess(() -> createTestValidator(schemaWithNot).validate(jsonStringValue("foo")));
    }
}
