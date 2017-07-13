package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.NullSchema;
import org.everit.json.JsonElement;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class NullSchemaValidator extends SchemaValidator<NullSchema> {
    public NullSchemaValidator(NullSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> toBeValidated) {
        checkNotNull(toBeValidated, "toBeValidated must not be null");
        if (toBeValidated.schemaType() != JsonSchemaType.Null) {
            return Optional.of(failure("expected: null, found: " + toBeValidated.schemaType(), "type"));
        }
        return Optional.empty();
    }
}
