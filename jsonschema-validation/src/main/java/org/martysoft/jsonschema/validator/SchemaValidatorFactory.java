package org.martysoft.jsonschema.validator;

import org.martysoft.jsonschema.v6.ArraySchema;
import org.martysoft.jsonschema.v6.BooleanSchema;
import org.martysoft.jsonschema.v6.CombinedSchema;
import org.martysoft.jsonschema.v6.EmptySchema;
import org.martysoft.jsonschema.v6.EnumSchema;
import org.martysoft.jsonschema.v6.NotSchema;
import org.martysoft.jsonschema.v6.NullSchema;
import org.martysoft.jsonschema.v6.NumberSchema;
import org.martysoft.jsonschema.v6.ObjectSchema;
import org.martysoft.jsonschema.v6.ReferenceSchema;
import org.martysoft.jsonschema.v6.Schema;
import org.martysoft.jsonschema.v6.StringSchema;

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
