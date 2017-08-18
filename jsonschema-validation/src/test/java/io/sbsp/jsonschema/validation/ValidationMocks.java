package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;

import java.net.URI;

import static io.sbsp.jsonschema.JsonSchemaProvider.schemaBuilder;

public class ValidationMocks {

    public static SchemaValidator createTestValidator(SchemaBuilder schema) {
        return createTestValidator(schema.build());
    }

    public static SchemaValidator createTestValidator(Schema schema) {
        return SchemaValidatorFactoryImpl.builder().build().createValidator(schema);
    }

    public static SchemaBuilder mockArraySchema() {
        return schemaBuilder().type(JsonSchemaType.ARRAY);
    }

    public static SchemaBuilder mockBooleanSchema() {
        return schemaBuilder().type(JsonSchemaType.BOOLEAN);
    }

    public static SchemaBuilder mockBooleanSchema(URI id) {
        return schemaBuilder(id).type(JsonSchemaType.BOOLEAN);
    }

    public static SchemaBuilder mockBooleanSchema(String id) {
        return schemaBuilder(id).type(JsonSchemaType.BOOLEAN);
    }

    public static SchemaBuilder mockIntegerSchema() {
        return schemaBuilder().type(JsonSchemaType.INTEGER);
    }

    public static SchemaBuilder mockNullSchema() {
        return schemaBuilder().type(JsonSchemaType.NULL);
    }

    public static SchemaBuilder mockNumberSchema() {
        return schemaBuilder().type(JsonSchemaType.NUMBER);
    }

    public static SchemaBuilder mockObjectSchema() {
        return schemaBuilder().type(JsonSchemaType.OBJECT);
    }

    public static SchemaBuilder mockObjectSchema(String id) {
        return schemaBuilder(id).type(JsonSchemaType.OBJECT);
    }
    
    public static SchemaBuilder mockObjectSchema(URI id) {
        return schemaBuilder(id).type(JsonSchemaType.OBJECT);
    }

    public static SchemaBuilder mockSchema() {
        return schemaBuilder();
    }

    public static SchemaBuilder mockStringSchema() {
        return schemaBuilder().type(JsonSchemaType.STRING);
    }
}
