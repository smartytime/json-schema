package io.dugnutt.jsonschema.six;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public enum JsonSchemaKeyword {
    $SCHEMA,
    $REF,
    $ID,
    TITLE,
    DEFINITIONS,
    DESCRIPTION,

    /**
     * <section title='"default"'>
     * <t>
     * There are no restrictions placed on the value of this keyword.
     * </t>
     * <t>
     * This keyword can be used to supply a default JSON value associated with a
     * particular schema. It is RECOMMENDED that a default value be valid against
     * the associated schema.
     * </t>
     * </section>
     */
    DEFAULT,

    /**
     * <section title="properties">
     * <t>
     * The value of "properties" MUST be an object.
     * Each value of this object MUST be a valid JSON Schema.
     * </t>
     * <t>
     * This keyword determines how child instances validate for objects,
     * and does not directly validate the immediate instance itself.
     * </t>
     * <t>
     * Validation succeeds if, for each name that appears in both
     * the instance and as a name within this keyword's value, the child
     * instance for that name successfully validates against the
     * corresponding schema.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as an empty object.
     * </t>
     * </section>
     */
    PROPERTIES(JsonSchemaType.OBJECT),

    /**
     * <section title="type">
     * <t>
     * The value of this keyword MUST be either a string or an array. If it is
     * an array, elements of the array MUST be strings and MUST be unique.
     * </t>
     * <t>
     * String values MUST be one of the six primitive types
     * ("null", "boolean", "object", "array", "number", or "string"),
     * or "integer" which matches any number with a zero fractional part.
     * </t>
     * <t>
     * An instance validates if and only if the instance is in any of the sets listed
     * for this keyword.
     * </t>
     * </section>
     */
    TYPE(true),

    /**
     * <section title="multipleOf">
     * <t>
     * The value of "multipleOf" MUST be a number, strictly greater than 0.
     * </t>
     * <t>
     * A numeric instance is valid only if division by this keyword's value results in
     * an integer.
     * </t>
     * </section>
     */
    MULTIPLE_OF(JsonSchemaType.NUMBER),

    /**
     * <section title="maximum">
     * <t>
     * The value of "maximum" MUST be a number, representing an inclusive upper limit
     * for a numeric instance.
     * </t>
     * <t>
     * If the instance is a number, then this keyword validates only if the instance is
     * less than or exactly equal to "maximum".
     * </t>
     * </section>
     */
    MAXIMUM(JsonSchemaType.NUMBER),

    /**
     * <section title="exclusiveMaximum">
     * <t>
     * The value of "exclusiveMaximum" MUST be number, representing an exclusive upper
     * limit for a numeric instance.
     * </t>
     * <t>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly less than (not equal to) "exclusiveMaximum".
     * </t>
     * </section>
     */
    EXCLUSIVE_MAXIMUM(JsonSchemaType.NUMBER),

    /**
     * <section title="minimum">
     * <t>
     * The value of "minimum" MUST be a number, representing an inclusive lower limit
     * for a numeric instance.
     * </t>
     * <t>
     * If the instance is a number, then this keyword validates only if the instance is
     * greater than or exactly equal to "minimum".
     * </t>
     * </section>
     */
    MINIMUM(JsonSchemaType.NUMBER),

    /**
     * <section title="exclusiveMinimum">
     * <t>
     * The value of "exclusiveMinimum" MUST be number, representing an exclusive lower
     * limit for a numeric instance.
     * </t>
     * <t>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly greater than (not equal to) "exclusiveMinimum".
     * </t>
     * </section>
     */
    EXCLUSIVE_MINIMUM(JsonSchemaType.NUMBER),

    /**
     * <section title="maxLength">
     * <t>
     * The value of this keyword MUST be a non-negative integer.</t>
     * <t>
     * A string instance is valid against this keyword if its
     * length is less than, or equal to, the value of this keyword.
     * </t>
     * <t>
     * The length of a string instance is defined as the number of its
     * characters as defined by <xref target="RFC7159">RFC 7159</xref>.
     * </t>
     * </section>
     */
    MAX_LENGTH(JsonSchemaType.STRING),

    /**
     * <section title="minLength">
     * <t>
     * The value of this keyword MUST be a non-negative integer.
     * </t>
     * <t>
     * A string instance is valid against this keyword if its
     * length is greater than, or equal to, the value of this keyword.
     * </t>
     * <p>
     * <t>
     * The length of a string instance is defined as the number of its
     * characters as defined by <xref target="RFC7159">RFC 7159</xref>.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as a value of 0.
     * </t>
     * </section>
     */
    MIN_LENGTH(JsonSchemaType.STRING),

    /**
     * <section title="pattern">
     * <t>
     * The value of this keyword MUST be a string. This string SHOULD be a
     * valid regular expression, according to the ECMA 262 regular expression
     * dialect.
     * </t>
     * <t>
     * A string instance is considered valid if the regular
     * expression matches the instance successfully. Recall: regular
     * expressions are not implicitly anchored.
     * </t>
     * </section>
     */
    PATTERN(JsonSchemaType.STRING),

    /**
     * <section title="items">
     * <t>
     * The value of "items" MUST be either a valid JSON Schema or an array of valid
     * JSON Schemas.
     * </t>
     * <t>
     * This keyword determines how child instances validate for arrays,
     * and does not directly validate the immediate instance itself.
     * </t>
     * <t>
     * If "items" is a schema, validation succeeds if all elements
     * in the array successfully validate against that schema.
     * </t>
     * <t>
     * If "items" is an array of schemas, validation succeeds if
     * each element of the instance validates against the schema at the
     * same position, if any.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as an empty schema.
     * </t>
     * </section>
     */
    ITEMS(JsonSchemaType.ARRAY),

    /**
     * <section title="additionalItems">
     * <t>
     * The value of "additionalItems" MUST be a valid JSON Schema.
     * </t>
     * <t>
     * This keyword determines how child instances validate for arrays,
     * and does not directly validate the immediate instance itself.
     * </t>
     * <t>
     * If "items" is an array of schemas, validation succeeds
     * if every instance element at a position greater than the size
     * of "items" validates against "additionalItems".
     * </t>
     * <t>
     * Otherwise, "additionalItems" MUST be ignored, as the "items"
     * schema (possibly the default value of an empty schema) is
     * applied to all elements.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as an empty schema.
     * </t>
     * </section>
     */
    ADDITIONAL_ITEMS(JsonSchemaType.ARRAY),

    /**
     * <section title="maxItems">
     * <t>
     * The value of this keyword MUST be a non-negative integer.
     * </t>
     * <t>
     * An array instance is valid against "maxItems" if its size is
     * less than, or equal to, the value of this keyword.
     * </t>
     * </section>
     */
    MAX_ITEMS(JsonSchemaType.ARRAY),

    /**
     * <section title="minItems">
     * <t>
     * The value of this keyword MUST be a non-negative integer.
     * </t>
     * <t>
     * An array instance is valid against "minItems" if its size is
     * greater than, or equal to, the value of this keyword.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as a value of 0.
     * </t>
     * </section>
     */
    MIN_ITEMS(JsonSchemaType.ARRAY),

    /**
     * <section title="uniqueItems">
     * <t>
     * The value of this keyword MUST be a boolean.
     * </t>
     * <t>
     * If this keyword has boolean value false, the instance validates
     * successfully. If it has boolean value true, the instance validates
     * successfully if all of its elements are unique.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as a value of false.
     * </t>
     * </section>
     */
    UNIQUE_ITEMS(JsonSchemaType.ARRAY),

    /**
     * <section title="contains">
     * <t>
     * The value of this keyword MUST be a valid JSON Schema.
     * </t>
     * <t>
     * An array instance is valid against "contains" if at least one of
     * its elements is valid against the given schema.
     * </t>
     * </section>
     */
    CONTAINS(JsonSchemaType.ARRAY),

    /**
     * <section title="maxProperties">
     * <t>
     * The value of this keyword MUST be a non-negative integer.
     * </t>
     * <t>
     * An object instance is valid against "maxProperties" if its
     * number of properties is less than, or equal to, the value of this
     * keyword.
     * </t>
     * </section>
     */
    MAX_PROPERTIES(JsonSchemaType.OBJECT),
    /**
     * <section title="required">
     * <t>
     * The value of this keyword MUST be an array.
     * Elements of this array, if any, MUST be strings, and MUST be unique.
     * </t>
     * <t>
     * An object instance is valid against this keyword if every item in the array is
     * the name of a property in the instance.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as an empty array.
     * </t>
     * </section>
     */
    REQUIRED(JsonSchemaType.OBJECT),
    /**
     * <section title="enum">
     * <t>
     * The value of this keyword MUST be an array. This array SHOULD have at
     * least one element. Elements in the array SHOULD be unique.
     * </t>
     * <t>
     * An instance validates successfully against this keyword if its value is
     * equal to one of the elements in this keyword's array value.
     * </t>
     * <t>
     * Elements in the array might be of any value, including null.
     * </t>
     * </section>
     */
    ENUM(true),
    /**
     * <section title="format">
     * <t>
     * Structural validation alone may be insufficient to validate that an instance
     * meets all the requirements of an application. The "format" keyword is defined to
     * allow interoperable semantic validation for a fixed subset of values which are
     * accurately described by authoritative resources, be they RFCs or other external
     * specifications.
     * </t>
     * <p>
     * <t>
     * The value of this keyword is called a format attribute. It MUST be a string. A
     * format attribute can generally only validate a given set of instance types. If
     * the type of the instance to validate is not in this set, validation for this
     * format attribute and instance SHOULD succeed.
     * </t>
     * <p>
     * </section>
     */
    FORMAT(JsonSchemaType.STRING),

    /**
     * <section title="const">
     * <t>
     * The value of this keyword MAY be of any type, including null.
     * </t>
     * <t>
     * An instance validates successfully against this keyword if its value is
     * equal to the value of the keyword.
     * </t>
     * </section>
     */
    CONST(true),

    /**
     * <section title="additionalProperties">
     * <t>
     * The value of "additionalProperties" MUST be a valid JSON Schema.
     * </t>
     * <t>
     * This keyword determines how child instances validate for objects,
     * and does not directly validate the immediate instance itself.
     * </t>
     * <t>
     * Validation with "additionalProperties" applies only to the child
     * values of instance names that do not match any names in "properties",
     * and do not match any regular expression in "patternProperties".
     * </t>
     * <t>
     * For all such properties, validation succeeds if the child instance
     * validates against the "additionalProperties" schema.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as an empty schema.
     * </t>
     * </section>
     */
    ADDITIONAL_PROPERTIES(JsonSchemaType.OBJECT),

    /**
     * <section title="minProperties">
     * <t>
     * The value of this keyword MUST be a non-negative integer.
     * </t>
     * <t>
     * An object instance is valid against "minProperties" if its
     * number of properties is greater than, or equal to, the value of this
     * keyword.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as a value of 0.
     * </t>
     * </section>
     */
    MIN_PROPERTIES(JsonSchemaType.OBJECT),

    /**
     * <section title="dependencies">
     * <t>
     * This keyword specifies rules that are evaluated if the instance is an object and
     * contains a certain property.
     * </t>
     * <t>
     * This keyword's value MUST be an object. Each property specifies a dependency.
     * Each dependency value MUST be an array or a valid JSON Schema.
     * </t>
     * <t>
     * If the dependency value is a subschema, and the dependency key is a property
     * in the instance, the entire instance must validate against the dependency value.
     * </t>
     * <t>
     * If the dependency value is an array, each element in the array,
     * if any, MUST be a string, and MUST be unique. If the dependency key is
     * a property in the instance, each of the items in the dependency
     * value must be a property that exists in the instance.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as an empty object.
     * </t>
     * </section>
     */
    DEPENDENCIES(JsonSchemaType.OBJECT),
    /**
     * <section title="patternProperties">
     * <t>
     * The value of "patternProperties" MUST be an object. Each property name
     * of this object SHOULD be a valid regular expression, according to the
     * ECMA 262 regular expression dialect. Each property value of this object
     * MUST be a valid JSON Schema.
     * </t>
     * <t>
     * This keyword determines how child instances validate for objects,
     * and does not directly validate the immediate instance itself.
     * Validation of the primitive instance type against this keyword
     * always succeeds.
     * </t>
     * <t>
     * Validation succeeds if, for each instance name that matches any
     * regular expressions that appear as a property name in this keyword's value,
     * the child instance for that name successfully validates against each
     * schema that corresponds to a matching regular expression.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as an empty object.
     * </t>
     * </section>
     */
    PATTERN_PROPERTIES(JsonSchemaType.OBJECT),
    /**
     * <section title="propertyNames">
     * <t>
     * The value of "propertyNames" MUST be a valid JSON Schema.
     * </t>
     * <t>
     * If the instance is an object, this keyword validates if every property name in
     * the instance validates against the provided schema.
     * Note the property name that the schema is testing will always be a string.
     * </t>
     * <t>
     * Omitting this keyword has the same behavior as an empty schema.
     * </t>
     * </section>
     */
    PROPERTY_NAMES(JsonSchemaType.OBJECT),
    /**
     * <section title="not">
     * <t>
     * This keyword's value MUST be a valid JSON Schema.
     * </t>
     * <t>
     * An instance is valid against this keyword if it fails to validate
     * successfully against the schema defined by this keyword.
     * </t>
     * </section>
     */
    NOT,
    /**
     * <section title="allOf">
     * <t>
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * </t>
     * <t>
     * An instance validates successfully against this keyword if it validates
     * successfully against all schemas defined by this keyword's value.
     * </t>
     * </section>
     */
    ALL_OF(true),
    /**
     * <section title="anyOf">
     * <t>
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * </t>
     * <t>
     * An instance validates successfully against this keyword if it validates
     * successfully against at least one schema defined by this keyword's value.
     * </t>
     * </section>
     */
    ANY_OF(true),
    /**
     * <section title="oneOf">
     * <t>
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * </t>
     * <t>
     * An instance validates successfully against this keyword if it validates
     * successfully against exactly one schema defined by this keyword's value.
     * </t>
     * </section>
     */
    ONE_OF(true);

    private final String key;
    private final Set<JsonSchemaType> appliesTo;
    private final boolean appliesToAll;

    JsonSchemaKeyword(boolean appliesToAll, JsonSchemaType... allowedTypes) {
        this.appliesToAll = appliesToAll;
        this.key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
        this.appliesTo = Collections.unmodifiableSet(Sets.newHashSet(allowedTypes));
    }

    JsonSchemaKeyword(JsonSchemaType... appliesToTypes) {
        this(false, appliesToTypes);
    }

    public boolean appliesToType(JsonSchemaType type) {
        return appliesTo.contains(type);
    }

    public String key() {
        return key;
    }

}
