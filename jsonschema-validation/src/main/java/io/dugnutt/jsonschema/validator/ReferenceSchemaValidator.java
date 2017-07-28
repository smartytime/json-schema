package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;
import lombok.Builder;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class ReferenceSchemaValidator implements SchemaValidator {

    private final AtomicReference<SchemaValidator> refValidator = new AtomicReference<>();

    @Builder
    public ReferenceSchemaValidator(SchemaValidatorFactory validatorFactory, ReferenceSchema referenceSchema) {
        this.validatorFactory = checkNotNull(validatorFactory);
        this.referenceSchema = checkNotNull(referenceSchema);
    }

    @NonNull
    private final SchemaValidatorFactory validatorFactory;

    @NonNull
    private final ReferenceSchema referenceSchema;

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        final ValidationReport refReport = new ValidationReport();
        final SchemaValidator validator = getRefValidator();
        validator.validate(subject, refReport);
        boolean success = true;
        for (ValidationError validationError : refReport.getErrors()) {
            report.addError(validationError.toBuilder()
                    .schemaLocation(referenceSchema.getLocation().getJsonPointerFragment())
                    .violatedSchema(referenceSchema).build());

            success = false;
        }
        return success;
    }

    @Override
    public Schema getSchema() {
        return referenceSchema;
    }

    public SchemaValidator getRefValidator() {
        if (refValidator.get() == null) {
            synchronized (refValidator) {
                if (refValidator.get() == null) {
                    final Schema refSchema = referenceSchema.getRefSchema()
                            .orElseThrow(() -> new IllegalStateException("Target ref hasn't been loaded."));
                    final SchemaValidator validator = validatorFactory.createValidator(refSchema);
                    if (validator instanceof ReferenceSchemaValidator) {
                        throw new IllegalStateException("Target should not be reference");
                    }
                    refValidator.set(validator);
                }
            }
        }
        return refValidator.get();
    }

    @Override
    public String toString() {
        return "refSchemaValidator: " + referenceSchema.getRefURI();
    }
}
