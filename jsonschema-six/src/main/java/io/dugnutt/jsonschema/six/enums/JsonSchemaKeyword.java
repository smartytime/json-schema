package io.dugnutt.jsonschema.six.enums;

import com.google.common.base.CaseFormat;

public enum JsonSchemaKeyword {
    $SCHEMA,
    $REF,
    $ID,
    TITLE,
    DEFINITIONS,
    DESCRIPTION,

    /**
     * There are no restrictions placed on the value of this keyword.
     * <p>
     * This keyword can be used to supply a default JSON value associated with a
     * particular schema. It is RECOMMENDED that a default value be valid against
     * the associated schema.
     */
    DEFAULT,

    /**
     * The value of "properties" MUST be an object.
     * Each value of this object MUST be a valid JSON Schema.
     * <p>
     * This keyword determines how child instances validate for objects,
     * and does not directly validate the immediate instance itself.
     * <p>
     * Validation succeeds if, for each name that appears in both
     * the instance and as a name within this keyword's value, the child
     * instance for that name successfully validates against the
     * corresponding schema.
     * <p>
     * Omitting this keyword has the same behavior as an empty object.
     */
    PROPERTIES,

    /**
     * The value of this keyword MUST be either a string or an array. If it is
     * an array, elements of the array MUST be strings and MUST be unique.
     * <p>
     * String values MUST be one of the six primitive types
     * ,
     * or "integer" which matches any number with a zero fractional part.
     * <p>
     * An instance validates if and only if the instance is in any of the sets listed
     * for this keyword.
     */
    TYPE,

    /**
     * The value of "multipleOf" MUST be a number, strictly greater than 0.
     * <p>
     * A numeric instance is valid only if division by this keyword's value results in
     * an integer.
     */
    MULTIPLE_OF,

    /**
     * The value of "maximum" MUST be a number, representing an inclusive upper limit
     * for a numeric instance.
     * <p>
     * If the instance is a number, then this keyword validates only if the instance is
     * less than or exactly equal to "maximum".
     */
    MAXIMUM,

    /**
     * The value of "exclusiveMaximum" MUST be number, representing an exclusive upper
     * limit for a numeric instance.
     * <p>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly less than (not equal to) "exclusiveMaximum".
     */
    EXCLUSIVE_MAXIMUM,

    /**
     * The value of "minimum" MUST be a number, representing an inclusive lower limit
     * for a numeric instance.
     * <p>
     * If the instance is a number, then this keyword validates only if the instance is
     * greater than or exactly equal to "minimum".
     */
    MINIMUM,

    /**
     * The value of "exclusiveMinimum" MUST be number, representing an exclusive lower
     * limit for a numeric instance.
     * <p>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly greater than (not equal to) "exclusiveMinimum".
     */
    EXCLUSIVE_MINIMUM,

    /**
     * The value of this keyword MUST be a non-negative integer.
     * A string instance is valid against this keyword if its
     * length is less than, or equal to, the value of this keyword.
     * <p>
     * The length of a string instance is defined as the number of its
     * characters as defined by <a href="https://tools.ietf.org/html/RFC7159">RFC 7159</a>.
     */
    MAX_LENGTH,

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * A string instance is valid against this keyword if its
     * length is greater than, or equal to, the value of this keyword.
     * a href
     * <p>
     * The length of a string instance is defined as the number of its
     * characters as defined by <a href="https://tools.ietf.org/html/RFC7159">RFC 7159</a>.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    MIN_LENGTH,

    /**
     * The value of this keyword MUST be a string. This string SHOULD be a
     * valid regular expression, according to the ECMA 262 regular expression
     * dialect.
     * <p>
     * A string instance is considered valid if the regular
     * expression matches the instance successfully. Recall: regular
     * expressions are not implicitly anchored.
     */
    PATTERN,

    /**
     * The value of "items" MUST be either a valid JSON Schema or an array of valid
     * JSON Schemas.
     * <p>
     * This keyword determines how child instances validate for arrays,
     * and does not directly validate the immediate instance itself.
     * <p>
     * If "items" is a schema, validation succeeds if all elements
     * in the array successfully validate against that schema.
     * <p>
     * If "items" is an array of schemas, validation succeeds if
     * each element of the instance validates against the schema at the
     * same position, if any.
     * <p>
     * Omitting this keyword has the same behavior as an empty schema.
     */
    ITEMS,

    /**
     * The value of "additionalItems" MUST be a valid JSON Schema.
     * <p>
     * This keyword determines how child instances validate for arrays,
     * and does not directly validate the immediate instance itself.
     * <p>
     * If "items" is an array of schemas, validation succeeds
     * if every instance element at a position greater than the size
     * of "items" validates against "additionalItems".
     * <p>
     * Otherwise, "additionalItems" MUST be ignored, as the "items"
     * schema (possibly the default value of an empty schema) is
     * applied to all elements.
     * <p>
     * Omitting this keyword has the same behavior as an empty schema.
     */
    ADDITIONAL_ITEMS,

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An array instance is valid against "maxItems" if its size is
     * less than, or equal to, the value of this keyword.
     */
    MAX_ITEMS,

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An array instance is valid against "minItems" if its size is
     * greater than, or equal to, the value of this keyword.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    MIN_ITEMS,

    /**
     * The value of this keyword MUST be a boolean.
     * <p>
     * If this keyword has boolean value false, the instance validates
     * successfully. If it has boolean value true, the instance validates
     * successfully if all of its elements are unique.
     * <p>
     * Omitting this keyword has the same behavior as a value of false.
     */
    UNIQUE_ITEMS,

    /**
     * The value of this keyword MUST be a valid JSON Schema.
     * <p>
     * An array instance is valid against "contains" if at least one of
     * its elements is valid against the given schema.
     */
    CONTAINS,

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An object instance is valid against "maxProperties" if its
     * number of properties is less than, or equal to, the value of this
     * keyword.
     */
    MAX_PROPERTIES,
    /**
     * The value of this keyword MUST be an array.
     * Elements of this array, if any, MUST be strings, and MUST be unique.
     * <p>
     * An object instance is valid against this keyword if every item in the array is
     * the name of a property in the instance.
     * <p>
     * Omitting this keyword has the same behavior as an empty array.
     */
    REQUIRED,
    /**
     * The value of this keyword MUST be an array. This array SHOULD have at
     * least one element. Elements in the array SHOULD be unique.
     * <p>
     * An instance validates successfully against this keyword if its value is
     * equal to one of the elements in this keyword's array value.
     * <p>
     * Elements in the array might be of any value, including null.
     */
    ENUM,
    /**
     * Structural validation alone may be insufficient to validate that an instance
     * meets all the requirements of an application. The "format" keyword is defined to
     * allow interoperable semantic validation for a fixed subset of values which are
     * accurately described by authoritative resources, be they RFCs or other external
     * specifications.
     * a href
     * <p>
     * The value of this keyword is called a format attribute. It MUST be a string. A
     * format attribute can generally only validate a given set of instance types. If
     * the type of the instance to validate is not in this set, validation for this
     * format attribute and instance SHOULD succeed.
     * a href
     */
    FORMAT,

    /**
     * The value of this keyword MAY be of any type, including null.
     * <p>
     * An instance validates successfully against this keyword if its value is
     * equal to the value of the keyword.
     */
    CONST,

    /**
     * The value of "additionalProperties" MUST be a valid JSON Schema.
     * <p>
     * This keyword determines how child instances validate for objects,
     * and does not directly validate the immediate instance itself.
     * <p>
     * Validation with "additionalProperties" applies only to the child
     * values of instance names that do not match any names in "properties",
     * and do not match any regular expression in "patternProperties".
     * <p>
     * For all such properties, validation succeeds if the child instance
     * validates against the "additionalProperties" schema.
     * <p>
     * Omitting this keyword has the same behavior as an empty schema.
     */
    ADDITIONAL_PROPERTIES,

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An object instance is valid against "minProperties" if its
     * number of properties is greater than, or equal to, the value of this
     * keyword.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    MIN_PROPERTIES,

    /**
     * This keyword specifies rules that are evaluated if the instance is an object and
     * contains a certain property.
     * <p>
     * This keyword's value MUST be an object. Each property specifies a dependency.
     * Each dependency value MUST be an array or a valid JSON Schema.
     * <p>
     * If the dependency value is a subschema, and the dependency key is a property
     * in the instance, the entire instance must validate against the dependency value.
     * <p>
     * If the dependency value is an array, each element in the array,
     * if any, MUST be a string, and MUST be unique. If the dependency key is
     * a property in the instance, each of the items in the dependency
     * value must be a property that exists in the instance.
     * <p>
     * Omitting this keyword has the same behavior as an empty object.
     */
    DEPENDENCIES,
    /**
     * The value of "patternProperties" MUST be an object. Each property name
     * of this object SHOULD be a valid regular expression, according to the
     * ECMA 262 regular expression dialect. Each property value of this object
     * MUST be a valid JSON Schema.
     * <p>
     * This keyword determines how child instances validate for objects,
     * and does not directly validate the immediate instance itself.
     * Validation of the primitive instance type against this keyword
     * always succeeds.
     * <p>
     * Validation succeeds if, for each instance name that matches any
     * regular expressions that appear as a property name in this keyword's value,
     * the child instance for that name successfully validates against each
     * schema that corresponds to a matching regular expression.
     * <p>
     * Omitting this keyword has the same behavior as an empty object.
     */
    PATTERN_PROPERTIES,
    /**
     * The value of "propertyNames" MUST be a valid JSON Schema.
     * <p>
     * If the instance is an object, this keyword validates if every property name in
     * the instance validates against the provided schema.
     * Note the property name that the schema is testing will always be a string.
     * <p>
     * Omitting this keyword has the same behavior as an empty schema.
     */
    PROPERTY_NAMES,
    /**
     * This keyword's value MUST be a valid JSON Schema.
     * <p>
     * An instance is valid against this keyword if it fails to validate
     * successfully against the schema defined by this keyword.
     */
    NOT,
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against all schemas defined by this keyword's value.
     */
    ALL_OF,
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against at least one schema defined by this keyword's value.
     */
    ANY_OF,
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against exactly one schema defined by this keyword's value.
     */
    ONE_OF;

    private final String key;

    JsonSchemaKeyword() {
        this.key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
    }

    public String key() {
        return key;
    }

    public String toString() {
        return key;
    }

}
