package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;

import java.util.function.Function;

import static io.sbsp.jsonschema.JsonSchemaProvider.*;

public class Schemas {
    private static SchemaBuilder EMPTY_SCHEMA_BUILDER = schemaBuilder();
    private static Schema EMPTY_SCHEMA = EMPTY_SCHEMA_BUILDER.build();
    private static SchemaBuilder NULL_SCHEMA_BUILDER = schemaBuilder().type(JsonSchemaType.NULL);
    private static Schema NULL_SCHEMA = NULL_SCHEMA_BUILDER.build();
    private static SchemaBuilder FALSE_SCHEMA_BUILDER = schemaBuilder().notSchema(EMPTY_SCHEMA_BUILDER);
    private static Schema FALSE_SCHEMA = FALSE_SCHEMA_BUILDER.build();

    public static Schema nullSchema() {
        return NULL_SCHEMA;
    }

    public static SchemaBuilder nullSchemaBuilder() {
        return NULL_SCHEMA_BUILDER;
    }

    public static Schema falseSchema() {
        return FALSE_SCHEMA;
    }

    public static SchemaBuilder falseSchemaBuilder() {
        return FALSE_SCHEMA_BUILDER;
    }

    public static Function<Schema, Boolean> isNullSchema() {
        return schema -> NULL_SCHEMA.equals(schema);
    }
    public static Function<Schema, Boolean> isFalseSchema() {
        return schema -> FALSE_SCHEMA.equals(schema);
    }
    public static Function<Schema, Boolean> isEmptySchema() {
        return schema -> EMPTY_SCHEMA.equals(schema);
    }

}
