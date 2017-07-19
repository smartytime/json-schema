package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.ReferenceSchemaLoader;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Optional;

public class ReferenceSchemaValidator extends SchemaValidator<ReferenceSchema> {

    private final ReferenceSchemaLoader refLoader;

    public ReferenceSchemaValidator(ReferenceSchema schema) {
        super(schema);
        refLoader = null;
    }

    @Override
    public Optional<ValidationError> validate(JsonValue toBeValidated) {
        Schema refSchema = this.schema.getReferredSchema()
                .orElseGet(() -> refLoader.loadReferenceSchema(schema));

        if (refSchema == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }

        return SchemaValidatorFactory.createValidatorForSchema(refSchema)
                .validate(toBeValidated);
    }
}
