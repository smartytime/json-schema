package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.ReferenceSchema;

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
        return SchemaValidatorFactory.createValidatorForSchema(schema.getReferredSchema())
                .validate(toBeValidated);
    }
}
