package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.Schema;
import org.junit.Test;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.JsonSchemaProvider.schemaBuilder;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.sbsp.jsonschema.validation.ValidationMocks.createTestValidator;
import static io.sbsp.jsonschema.validation.ValidationMocks.mockBooleanSchema;
import static io.sbsp.jsonschema.validation.ValidationTestSupport.expectSuccess;

public class BaseSchemaValidatorNotSchemaTest {
    @Test
    public void failure() {
        Schema subject = schemaBuilder().notSchema(mockBooleanSchema()).build();
        ValidationTestSupport.failureOf(subject)
                .validator(createTestValidator(subject))
                .input(JsonValue.TRUE)
                .expectedKeyword("not")
                .expect();
    }

    @Test
    public void success() {
        final Schema schemaWithNot = schemaBuilder().notSchema(mockBooleanSchema()).build();
        expectSuccess(() -> createTestValidator(schemaWithNot).validate(jsonStringValue("foo")));
    }
}
