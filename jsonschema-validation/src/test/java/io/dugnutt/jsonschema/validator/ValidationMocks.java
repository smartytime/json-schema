package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;

import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.DEFAULT_VALIDATOR;

public class ValidationMocks {

    public static JsonSchemaValidator createTestValidator(JsonSchemaBuilder schema) {
        return createTestValidator(schema.build());
    }

    public static JsonSchemaValidator createTestValidator(Schema schema) {
        return DEFAULT_VALIDATOR.createValidator(schema);
    }

    public static JsonSchemaBuilder mockArraySchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.ARRAY);
    }

    public static JsonSchemaBuilder mockBooleanSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.BOOLEAN);
    }

    public static JsonSchemaBuilder mockIntegerSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.INTEGER);
    }

    public static JsonSchemaBuilder mockNullSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.NULL);
    }

    public static JsonSchemaBuilder mockNumberSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.NUMBER);
    }

    public static JsonSchemaBuilder mockObjectSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.OBJECT);
    }

    public static JsonSchemaBuilder mockSchema() {
        return jsonSchemaBuilder();
    }

    public static JsonSchemaBuilder mockStringSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.STRING);
    }

    public static PathAwareJsonValue pathAware(JsonValue subject) {
        return new PathAwareJsonValue(subject, JsonPath.rootPath());
    }
}
