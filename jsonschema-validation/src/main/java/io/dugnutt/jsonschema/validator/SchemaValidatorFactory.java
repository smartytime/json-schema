package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.EmptySchema;
import io.dugnutt.jsonschema.six.EnumSchema;
import io.dugnutt.jsonschema.six.NotSchema;
import io.dugnutt.jsonschema.six.NullSchema;
import io.dugnutt.jsonschema.six.NumberSchema;
import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StringSchema;

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
