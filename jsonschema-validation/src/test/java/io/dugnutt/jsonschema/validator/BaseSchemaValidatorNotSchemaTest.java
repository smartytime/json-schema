package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.EmptySchema;
import io.dugnutt.jsonschema.six.Schema;
import org.junit.Test;

import javax.json.JsonValue;

import static io.dugnutt.jsonschema.validator.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockAlwaysSuccessfulValidator;

public class BaseSchemaValidatorNotTest {
    @Test
    public void failure() {
        Schema subject = ValidationTestSupport.buildWithLocation(EmptySchema.builder()
                .notSchema(BooleanSchema.BOOLEAN_SCHEMA));
        ValidationTestSupport.failureOf(subject)
                .validator(new BaseSchemaValidator<>(subject, mockAlwaysSuccessfulValidator()))
                .input(JsonValue.TRUE)
                .expectedKeyword("not")
                .expect();
    }

    @Test
    public void success() {
        final Schema notSchema = EmptySchema.builder().notSchema(BooleanSchema.BOOLEAN_SCHEMA).build();
        expectSuccess(() -> new BaseSchemaValidator<>(notSchema, mockAlwaysSuccessfulValidator()).validate(jsonStringValue("foo")));
    }
}
