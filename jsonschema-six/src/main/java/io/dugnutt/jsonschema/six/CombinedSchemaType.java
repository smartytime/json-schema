package io.dugnutt.jsonschema.six;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public enum CombinedSchemaType {
    AllOf, OneOf, AnyOf;

    public JsonSchemaKeyword getProperty() {
        switch (this) {
            case AllOf:
                return JsonSchemaKeyword.ALL_OF;
            case AnyOf:
                return JsonSchemaKeyword.ANY_OF;
            case OneOf:
                return JsonSchemaKeyword.ONE_OF;
            default:
                throw new IllegalStateException("Shouldn't ever see this");
        }
    }

    @Override
    public String toString() {
        return UPPER_CAMEL.to(LOWER_CAMEL, name());
    }
}
