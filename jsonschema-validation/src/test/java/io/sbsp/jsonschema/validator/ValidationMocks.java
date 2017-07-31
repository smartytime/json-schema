package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;

import java.net.URI;

import static io.sbsp.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.sbsp.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.sbsp.jsonschema.six.Schema.jsonSchemaBuilderWithId;

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
