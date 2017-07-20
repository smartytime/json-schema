package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.ReferenceSchemaLoader;
import io.dugnutt.jsonschema.six.Schema;

import java.util.Optional;

public class ReferenceSchemaValidator extends SchemaValidator<ReferenceSchema> {

    private final ReferenceSchemaLoader refLoader;

    public ReferenceSchemaValidator(ReferenceSchema schema) {
        super(schema);
        refLoader = null;
    }

    public ReferenceSchemaValidator(ReferenceSchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
        this.refLoader = null;
    }

    public ReferenceSchemaValidator(ReferenceSchema schema, SchemaValidatorFactory factory, ReferenceSchemaLoader refLoader) {
        super(schema, factory);
        this.refLoader = refLoader;
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue toBeValidated) {
        Schema refSchema = this.schema.getReferredSchema()
                .orElseGet(() -> refLoader.loadReferenceSchema(schema));

        if (refSchema == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }

        return factory.createValidator(refSchema)
                .validate(toBeValidated);
    }
}
