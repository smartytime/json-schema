package org.everit.jsonschema.api;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public enum JsonSchemaProperty {

    // private static final List<String> ARRAY_SCHEMA_PROPS = asList("items", "additionalItems",
    //         "minItems",
    //         "maxItems",
    //         "uniqueItems");
    // private static final List<String> NUMBER_SCHEMA_PROPS = asList("minimum", "maximum",
    //         "minimumExclusive", "maximumExclusive", "multipleOf");
    // private static final List<String> OBJECT_SCHEMA_PROPS = asList("properties", "required",
    //         "minProperties",
    //         "maxProperties",
    //         "dependencies",
    //         "patternProperties",
    //         "additionalProperties");
    // private static final List<String> STRING_SCHEMA_PROPS = asList("minLength", "maxLength",
    //         "pattern", "format");
    //
    PROPERTIES(JsonSchemaType.Object),
    TYPE,
    REQUIRED(JsonSchemaType.Object),
    ENUM,
    FORMAT(JsonSchemaType.String),
    ID,
    DEFAULT,
    COMPONENT,
    DEFINITIONS,
    $SCHEMA,
    TITLE,
    ITEMS(JsonSchemaType.Array),
    ADDITIONAL_ITEMS(JsonSchemaType.Array),
    ADDITIONAL_PROPERTIES(JsonSchemaType.Object),
    MIN_ITEMS(JsonSchemaType.Array),
    MAX_ITEMS(JsonSchemaType.Array),
    MINIMUM(JsonSchemaType.Number),
    MAXIMUM(JsonSchemaType.Number),
    MINIMUM_EXCLUSIVE(JsonSchemaType.Number),
    MAXIMUM_EXCLUSIVE(JsonSchemaType.Number),
    MULTIPLE_OF(JsonSchemaType.Number),
    UNIQUE_ITEMS(JsonSchemaType.Array),
    MIN_PROPERTIES(JsonSchemaType.Object),
    MAX_PROPERTIES(JsonSchemaType.Object),
    DEPENDENCIES(JsonSchemaType.Object),
    PATTERN_PROPERTIES(JsonSchemaType.Object),
    MIN_LENGTH(JsonSchemaType.String),
    MAX_LENGTH(JsonSchemaType.String),
    PATTERN(JsonSchemaType.String),
    $REF,
    DESCRIPTION,
    NOT,
    EXCLUSIVE_MAXIMUM,
    EXCLUSIVE_MINIMUM,
    ALL_OF,
    ANY_OF,
    ONE_OF;

    private final String key;
    private final Set<JsonSchemaType> appliesTo;

    JsonSchemaProperty(JsonSchemaType... allowedTypes) {
        this.key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
        this.appliesTo = Collections.unmodifiableSet(Sets.newHashSet(allowedTypes));
    }

    JsonSchemaProperty(String key, JsonSchemaType... allowedTypes) {
        this.key = key;
        this.appliesTo = Collections.unmodifiableSet(Sets.newHashSet(allowedTypes));
    }

    public boolean appliesToType(JsonSchemaType type) {
        return appliesTo.contains(type);
    }

    public String getKey() {
        return key;
    }

}
