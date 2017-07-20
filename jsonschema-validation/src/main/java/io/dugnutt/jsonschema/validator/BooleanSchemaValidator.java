package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import java.util.Optional;

import static io.dugnutt.jsonschema.six.BooleanSchema.BOOLEAN_SCHEMA;
import static javax.json.JsonValue.ValueType;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.TRUE;

public class BooleanSchemaValidator extends SchemaValidator<BooleanSchema> {

    public static BooleanSchemaValidator BOOLEAN_SCHEMA_VALIDATOR = new BooleanSchemaValidator(BOOLEAN_SCHEMA);

    public BooleanSchemaValidator(BooleanSchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
    }

    public BooleanSchemaValidator(BooleanSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject) {

        final ValueType valueType = subject.getValueType();
        if (!subject.is(FALSE, TRUE)) {
            return buildTypeMismatchError(subject, JsonSchemaType.BOOLEAN).buildOptional();
        }

        return Optional.empty();
    }
}
