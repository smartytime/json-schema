package org.martysoft.jsonschema.validator;

import org.martysoft.jsonschema.v6.BooleanSchema;
import org.martysoft.jsonschema.v6.JsonSchemaType;

import javax.json.JsonValue;
import java.util.Optional;

import static javax.json.JsonValue.ValueType;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.TRUE;

public class BooleanSchemaValidator extends SchemaValidator<BooleanSchema> {

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
