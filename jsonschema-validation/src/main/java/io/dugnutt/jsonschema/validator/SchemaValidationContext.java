package io.dugnutt.jsonschema.validator;

import lombok.Builder;
import lombok.Getter;

import javax.json.spi.JsonProvider;

@Builder
@Getter
public class SchemaValidationContext {
    private final SchemaValidatorFactory factory;
    private final JsonProvider provider;

    public static SchemaValidationContext newContext() {
        return builder()
                .factory(SchemaValidatorFactory.DEFAULT_VALIDATOR)
                .provider(JsonProvider.provider())
                .build();
    }

    public JsonProvider getProvider() {
        return provider;
    }
}
