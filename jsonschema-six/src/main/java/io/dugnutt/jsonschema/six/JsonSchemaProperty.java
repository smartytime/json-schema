package io.dugnutt.jsonschema.six;

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
    PROPERTIES(JsonSchemaType.OBJECT),
    TYPE,
    REQUIRED(JsonSchemaType.OBJECT),
    ENUM,
    FORMAT(JsonSchemaType.STRING),
    ID,
    DEFAULT,
    COMPONENT,
    DEFINITIONS,
    $SCHEMA,
    TITLE,
    ITEMS(JsonSchemaType.ARRAY),
    ADDITIONAL_ITEMS(JsonSchemaType.ARRAY),
    ADDITIONAL_PROPERTIES(JsonSchemaType.OBJECT),
    MIN_ITEMS(JsonSchemaType.ARRAY),
    MAX_ITEMS(JsonSchemaType.ARRAY),
    MINIMUM(JsonSchemaType.NUMBER),
    MAXIMUM(JsonSchemaType.NUMBER),
    MINIMUM_EXCLUSIVE(JsonSchemaType.NUMBER),
    MAXIMUM_EXCLUSIVE(JsonSchemaType.NUMBER),
    MULTIPLE_OF(JsonSchemaType.NUMBER),
    UNIQUE_ITEMS(JsonSchemaType.ARRAY),
    MIN_PROPERTIES(JsonSchemaType.OBJECT),
    MAX_PROPERTIES(JsonSchemaType.OBJECT),
    DEPENDENCIES(JsonSchemaType.OBJECT),
    PATTERN_PROPERTIES(JsonSchemaType.OBJECT),
    MIN_LENGTH(JsonSchemaType.STRING),
    MAX_LENGTH(JsonSchemaType.STRING),
    PATTERN(JsonSchemaType.STRING),
    $REF,
    DESCRIPTION,
    NOT,
    EXCLUSIVE_MAXIMUM,
    EXCLUSIVE_MINIMUM,
    ALL_OF,
    ANY_OF,
    ONE_OF, NEEDS_UNIQUE_ITEMS, NEEDS_ADDITIONAL_ITEMS;

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

    public String key() {
        return key;
    }

}
