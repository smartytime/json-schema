package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import javax.json.JsonValue;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.JsonSchema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.JsonSchema.jsonSchemaBuilder;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.DEFAULT_VALIDATOR;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidationMocks {

    public static JsonSchemaValidator mockAlwaysSuccessfulValidator() {
        JsonSchemaValidator mockedValidator = mock(JsonSchemaValidator.class);
        Optional<ValidationError> empty = Optional.empty();
        when(mockedValidator.validate(any(PathAwareJsonValue.class))).thenReturn(empty);
        when(mockedValidator.validate(any(JsonValue.class))).thenReturn(empty);

        return mockedValidator;
    }

    public static PathAwareJsonValue pathAware(JsonValue subject) {
        return new PathAwareJsonValue(subject, JsonPath.rootPath());
    }

    public static JsonSchemaBuilder mockNullSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.NULL);
    }

    public static JsonSchemaBuilder mockBooleanSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.BOOLEAN);
    }

    public static JsonSchemaBuilder mockArraySchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.ARRAY);
    }

    public static JsonSchemaBuilder mockNumberSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.NUMBER);
    }

    public static JsonSchemaBuilder mockIntegerSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.INTEGER);
    }

    public static JsonSchemaValidator createTestValidator(JsonSchema schema) {
        return JsonSchemaValidator.jsonSchemaValidator()
                .factory(DEFAULT_VALIDATOR)
                .schema(schema)
                .build();
    }
}
