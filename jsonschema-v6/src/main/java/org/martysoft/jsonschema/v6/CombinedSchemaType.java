package org.martysoft.jsonschema.v6;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public enum CombinedSchemaType {
    AllOf, OneOf, AnyOf;

    public JsonSchemaProperty getProperty() {
        switch (this) {
            case AllOf:
                return JsonSchemaProperty.ALL_OF;
            case AnyOf:
                return JsonSchemaProperty.ANY_OF;
            case OneOf:
                return JsonSchemaProperty.ONE_OF;
            default:
                throw new IllegalStateException("Shouldn't ever see this");
        }
    }

    @Override
    public String toString() {
        return UPPER_CAMEL.to(LOWER_CAMEL, name());
    }
}
