package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.JsonSchemaType;

import javax.json.JsonValue;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.BooleanSchema.BOOLEAN_SCHEMA;
import static javax.json.JsonValue.ValueType;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.TRUE;

public class BooleanSchemaValidator extends SchemaValidator<BooleanSchema> {

    public static BooleanSchemaValidator BOOLEAN_SCHEMA_VALIDATOR = new BooleanSchemaValidator(BOOLEAN_SCHEMA);

    public BooleanSchemaValidator(BooleanSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue subject) {

        final ValueType valueType = subject.getValueType();
        if (valueType != FALSE && valueType != TRUE) {
            return Optional.of(failure(JsonSchemaType.BOOLEAN, JsonSchemaType.fromJsonType(valueType)));
        }

        return Optional.empty();
    }
}
