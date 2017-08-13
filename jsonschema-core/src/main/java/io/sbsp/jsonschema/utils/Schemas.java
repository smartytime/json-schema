package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;

import java.util.function.Function;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;

public class Schemas {
    private static Schema NULL_SCHEMA = JsonSchemaBuilder.jsonSchema().type(JsonSchemaType.NULL).build();

    public static Schema nullSchema() {
        return NULL_SCHEMA;
    }

    public static Function<Schema, Boolean> isNullSchema() {
        return schema -> NULL_SCHEMA.equals(schema);
    }

}
