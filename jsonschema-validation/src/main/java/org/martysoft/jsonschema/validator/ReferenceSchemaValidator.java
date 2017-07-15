package org.martysoft.jsonschema.validator;

import org.martysoft.jsonschema.v6.ReferenceSchema;

import javax.json.JsonValue;
import java.util.Optional;

public class ReferenceSchemaValidator extends SchemaValidator<ReferenceSchema> {

    public ReferenceSchemaValidator(ReferenceSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue toBeValidated) {
        if (schema.getReferredSchema() == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }
        return SchemaValidatorFactory.findValidator(schema.getReferredSchema())
                .validate(toBeValidated);
    }
}
