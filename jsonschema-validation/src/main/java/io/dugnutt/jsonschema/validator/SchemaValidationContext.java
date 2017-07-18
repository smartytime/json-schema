package io.dugnutt.jsonschema.validator;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SchemaValidationContext {
    private final SchemaValidatorFactory factory;

    public static SchemaValidationContext newContext() {
        return builder().factory(SchemaValidatorFactory.DEFAULT_VALIDATOR).build();
    }
}
