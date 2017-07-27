package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class NamedSchemaValidator implements SchemaValidator {
    @NonNull
    private final SchemaValidator wrapped;

    @NonNull
    private final String name;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        return wrapped.validate(subject, report);
    }
}
