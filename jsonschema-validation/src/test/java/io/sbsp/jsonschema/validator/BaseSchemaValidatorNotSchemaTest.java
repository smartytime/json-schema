package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.Schema;
import org.junit.Test;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.sbsp.jsonschema.validator.ValidationMocks.createTestValidator;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockBooleanSchema;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.expectSuccess;

public class BaseSchemaValidatorNotSchemaTest {
    @Test
    public void failure() {
        Schema subject = jsonSchema().notSchema(mockBooleanSchema()).build();
        ValidationTestSupport.failureOf(subject)
                .validator(createTestValidator(subject))
                .input(JsonValue.TRUE)
                .expectedKeyword("not")
                .expect();
    }

    @Test
    public void success() {
        final Schema schemaWithNot = jsonSchema().notSchema(mockBooleanSchema()).build();
        expectSuccess(() -> createTestValidator(schemaWithNot).validate(jsonStringValue("foo")));
    }
}
