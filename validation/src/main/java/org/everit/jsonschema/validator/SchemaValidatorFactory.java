package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.ArraySchema;
import org.everit.jsonschema.api.BooleanSchema;
import org.everit.jsonschema.api.CombinedSchema;
import org.everit.jsonschema.api.EmptySchema;
import org.everit.jsonschema.api.EnumSchema;
import org.everit.jsonschema.api.NotSchema;
import org.everit.jsonschema.api.NullSchema;
import org.everit.jsonschema.api.NumberSchema;
import org.everit.jsonschema.api.ObjectSchema;
import org.everit.jsonschema.api.ReferenceSchema;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.api.StringSchema;

public class SchemaValidatorFactory {
    public static SchemaValidator<?> findValidator(Schema schema) {
        if (schema instanceof ObjectSchema) {
            return new ObjectSchemaValidator((ObjectSchema) schema);
        } else if (schema instanceof ArraySchema) {
            return new ArraySchemaValidator((ArraySchema) schema);
        } else if (schema instanceof BooleanSchema) {
            return new BooleanSchemaValidator((BooleanSchema) schema);
        } else if (schema instanceof CombinedSchema) {
            return new CombinedSchemaValidator((CombinedSchema) schema);
        } else if (schema instanceof EmptySchema) {
            return new EmptySchemaValidator((EmptySchema) schema);
        } else if (schema instanceof EnumSchema) {
            return new EnumSchemaValidator((EnumSchema) schema);
        } else if (schema instanceof NotSchema) {
            return new NotSchemaValidator((NotSchema) schema);
        } else if (schema instanceof NullSchema) {
            return new NullSchemaValidator((NullSchema) schema);
        } else if (schema instanceof NumberSchema) {
            return new NumberSchemaValidator((NumberSchema) schema);
        } else if (schema instanceof ReferenceSchema) {
            return new ReferenceSchemaValidator((ReferenceSchema) schema);
        } else if (schema instanceof StringSchema) {
            return new StringSchemaValidator((StringSchema) schema);
        } else {
            throw new RuntimeException("Can't find validator for: " + schema.getClass().getSimpleName());
        }
    }
}
