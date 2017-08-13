package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;

import java.net.URI;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchemaBuilderWithId;

public class ValidationMocks {

    public static SchemaValidator createTestValidator(JsonSchemaBuilder schema) {
        return createTestValidator(schema.build());
    }

    public static SchemaValidator createTestValidator(Schema schema) {
        return SchemaValidatorFactory.builder().build().createValidator(schema);
    }

    public static JsonSchemaBuilder mockArraySchema() {
        return jsonSchema().type(JsonSchemaType.ARRAY);
    }

    public static JsonSchemaBuilder mockBooleanSchema() {
        return jsonSchema().type(JsonSchemaType.BOOLEAN);
    }

    public static JsonSchemaBuilder mockBooleanSchema(URI id) {
        return jsonSchemaBuilderWithId(id).type(JsonSchemaType.BOOLEAN);
    }

    public static JsonSchemaBuilder mockBooleanSchema(String id) {
        return jsonSchemaBuilderWithId(id).type(JsonSchemaType.BOOLEAN);
    }

    public static JsonSchemaBuilder mockIntegerSchema() {
        return jsonSchema().type(JsonSchemaType.INTEGER);
    }

    public static JsonSchemaBuilder mockNullSchema() {
        return jsonSchema().type(JsonSchemaType.NULL);
    }

    public static JsonSchemaBuilder mockNumberSchema() {
        return jsonSchema().type(JsonSchemaType.NUMBER);
    }

    public static JsonSchemaBuilder mockObjectSchema() {
        return jsonSchema().type(JsonSchemaType.OBJECT);
    }

    public static JsonSchemaBuilder mockObjectSchema(String id) {
        return jsonSchemaBuilderWithId(id).type(JsonSchemaType.OBJECT);
    }
    
    public static JsonSchemaBuilder mockObjectSchema(URI id) {
        return jsonSchemaBuilderWithId(id).type(JsonSchemaType.OBJECT);
    }

    public static JsonSchemaBuilder mockSchema() {
        return jsonSchema();
    }

    public static JsonSchemaBuilder mockStringSchema() {
        return jsonSchema().type(JsonSchemaType.STRING);
    }
}
