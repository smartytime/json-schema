package org.everit.jsonschema.api;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public enum CombinedSchemaType {
    AllOf, OneOf, AnyOf;

    @Override
    public String toString() {
        return UPPER_CAMEL.to(LOWER_CAMEL, name());
    }
}
