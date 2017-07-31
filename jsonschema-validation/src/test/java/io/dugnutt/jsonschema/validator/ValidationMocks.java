package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.enums.JsonSchemaType;

import java.net.URI;

import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilderWithId;

public class ValidationMocks {

    public static SchemaValidator createTestValidator(JsonSchemaBuilder schema) {
        return createTestValidator(schema.build());
    }

    public static SchemaValidator createTestValidator(Schema schema) {
        return SchemaValidatorFactory.builder().build().createValidator(schema);
    }

    public static JsonSchemaBuilder mockArraySchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.ARRAY);
    }

    public static JsonSchemaBuilder mockBooleanSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.BOOLEAN);
    }

    public static JsonSchemaBuilder mockBooleanSchema(URI id) {
        return jsonSchemaBuilderWithId(id).type(JsonSchemaType.BOOLEAN);
    }

    public static JsonSchemaBuilder mockBooleanSchema(String id) {
        return jsonSchemaBuilderWithId(id).type(JsonSchemaType.BOOLEAN);
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

    public static JsonSchemaBuilder mockObjectSchema(String id) {
        return jsonSchemaBuilderWithId(id).type(JsonSchemaType.OBJECT);
    }
    
    public static JsonSchemaBuilder mockObjectSchema(URI id) {
        return jsonSchemaBuilderWithId(id).type(JsonSchemaType.OBJECT);
    }

    public static JsonSchemaBuilder mockSchema() {
        return jsonSchemaBuilder();
    }

    public static JsonSchemaBuilder mockStringSchema() {
        return jsonSchemaBuilder().type(JsonSchemaType.STRING);
    }
}
