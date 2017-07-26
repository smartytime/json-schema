package io.dugnutt.jsonschema.six;

import java.util.regex.Pattern;

public class JsonSchemaRunner {
    public static void main(String[] args) {
        final Schema jsonSchema = Schema.jsonSchemaBuilder()
                .id("#/some/id")
                .type(JsonSchemaType.STRING)
                .pattern(Pattern.compile("^[a-z]+$]"))
                .anyOfSchema(
                        Schema.jsonSchemaBuilder().notSchema(
                                Schema.jsonSchemaBuilder().constValueString("bob_is_cool")))
                .anyOfSchema(Schema.jsonSchemaBuilder().constValueString("bob_is_rad"))
                .anyOfSchema(Schema.jsonSchemaBuilder()
                        .type(JsonSchemaType.OBJECT)
                        .propertySchema("firstName",
                                Schema.jsonSchemaBuilder()
                                        .type(JsonSchemaType.STRING)
                                        .maxLength(44)))
                .allOfSchema(Schema.jsonSchemaBuilder().minLength(3))
                .allOfSchema(Schema.jsonSchemaBuilder().maxLength(10))
                .allOfSchema(Schema.jsonSchemaBuilder().ref("#/definitions/id_name"))
                .build();

        System.out.println(jsonSchema.toString());
    }
}
