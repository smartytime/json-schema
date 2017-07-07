package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.ReferenceSchema;
import org.everit.json.JsonElement;

import java.util.Optional;

public class ReferenceSchemaValidator extends SchemaValidator<ReferenceSchema> {

    public ReferenceSchemaValidator(ReferenceSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> toBeValidated) {
        if (schema.getReferredSchema() == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }
        return SchemaValidatorFactory.findValidator(schema.getReferredSchema())
                .validate(toBeValidated);
    }
}
