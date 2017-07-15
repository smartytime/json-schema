package org.martysoft.jsonschema.validator;

import org.martysoft.jsonschema.v6.NullSchema;

import javax.json.JsonValue;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class NullSchemaValidator extends SchemaValidator<NullSchema> {
    public NullSchemaValidator(NullSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue toBeValidated) {
        checkNotNull(toBeValidated, "toBeValidated must not be null");
        if (toBeValidated.getValueType() != JsonValue.ValueType.NULL) {
            return Optional.of(failure("expected: null, found: " + toBeValidated.getValueType(), "type"));
        }
        return Optional.empty();
    }
}
