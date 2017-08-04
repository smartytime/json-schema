package io.sbsp.jsonschema.keyword.keywords.shared;

import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class TypeKeyword implements SchemaKeyword {

    private final Set<JsonSchemaType> types;

    public TypeKeyword(JsonSchemaType first, JsonSchemaType... additionalTypes) {
        checkNotNull(first, "first must not be null");
        checkNotNull(additionalTypes, "additionalTypes must not be null");
        types = Collections.unmodifiableSet(EnumSet.of(first, additionalTypes));
    }

    public TypeKeyword(Set<JsonSchemaType> requiredTypes) {
        checkNotNull(requiredTypes, "requiredTypes must not be null");
        this.types = Collections.unmodifiableSet(requiredTypes);
    }

    public Set<JsonSchemaType> getTypes() {
        return types;
    }

    public TypeKeyword withAdditionalType(JsonSchemaType another) {
        checkNotNull(another, "another must not be null");
        return new TypeKeyword(another, types.toArray(new JsonSchemaType[0]));
    }
}
