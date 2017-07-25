package io.dugnutt.jsonschema.six;

import java.util.regex.Pattern;

public class JsonSchemaRunner {
    public static void main(String[] args) {
        final JsonSchema jsonSchema = JsonSchema.jsonSchemaBuilder()
                .id("#/some/id")
                .type(JsonSchemaType.STRING)
                .pattern(Pattern.compile("^[a-z]+$]"))
                .anyOfSchema(
                        JsonSchema.jsonSchemaBuilder().notSchema(
                                JsonSchema.jsonSchemaBuilder().constValueString("bob_is_cool")))
                .anyOfSchema(JsonSchema.jsonSchemaBuilder().constValueString("bob_is_rad"))
                .anyOfSchema(JsonSchema.jsonSchemaBuilder()
                        .type(JsonSchemaType.OBJECT)
                        .propertySchema("firstName",
                                JsonSchema.jsonSchemaBuilder()
                                        .type(JsonSchemaType.STRING)
                                        .maxLength(44)))
                .allOfSchema(JsonSchema.jsonSchemaBuilder().minLength(3))
                .allOfSchema(JsonSchema.jsonSchemaBuilder().maxLength(10))
                .allOfSchema(JsonSchema.jsonSchemaBuilder().ref("#/definitions/id_name"))
                .build();

        System.out.println(jsonSchema.toString());
    }
}
