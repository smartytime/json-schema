package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.BooleanSchema;
import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.json.JsonElement;

import java.util.Optional;

public class BooleanSchemaValidator extends SchemaValidator<BooleanSchema> {

    public BooleanSchemaValidator(BooleanSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> toBeValidated) {

        if (toBeValidated.schemaType() != JsonSchemaType.Boolean) {
            return Optional.of(failure(JsonSchemaType.Boolean, toBeValidated.schemaType()));
        }

        return Optional.empty();
    }

}
