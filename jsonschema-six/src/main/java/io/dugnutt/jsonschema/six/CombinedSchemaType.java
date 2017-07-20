package io.dugnutt.jsonschema.six;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

public enum CombinedSchemaType {
    ALL_OF, ONE_OF, ANY_OF;

    public JsonSchemaKeyword getKeyword() {
        switch (this) {
            case ALL_OF:
                return JsonSchemaKeyword.ALL_OF;
            case ANY_OF:
                return JsonSchemaKeyword.ANY_OF;
            case ONE_OF:
                return JsonSchemaKeyword.ONE_OF;
            default:
                throw new IllegalStateException("Shouldn't ever see this");
        }
    }

    @Override
    public String toString() {
        return UPPER_UNDERSCORE.to(LOWER_CAMEL, name());
    }
}
