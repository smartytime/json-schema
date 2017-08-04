package io.sbsp.jsonschema.enums;

import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.KeywordMetadata.KeywordMetadataBuilder;
import lombok.experimental.Delegate;

import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft4;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft5;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft6;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.keywordMetadata;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NULL;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;

/**
 * Represents each valid keyword in the json-schema specification.
 */
public enum JsonSchemaKeywordType {
    $SCHEMA(keywordMetadata().expects(STRING)),
    /**
     * From draft-06
     * <p>
     * The "$ref" keyword is used to reference a schema, and provides the ability to
     * validate recursive structures through self-reference.
     * <p>
     * An object schema with a "$ref" property MUST be interpreted as a "$ref" reference.
     * The value of the "$ref" property MUST be a URI Reference. Resolved against the current
     * URI base, it identifies the URI of a schema to use. All other properties in a "$ref"
     * object MUST be ignored.
     * <p>
     * The URI is not a network locator, only an identifier. A schema need not be downloadable
     * from the address if it is a network-addressable URL, and implementations SHOULD NOT assume
     * they should perform a network operation when they encounter a network-addressable URI.
     * <p>
     * A schema MUST NOT be run into an infinite loop against a schema. For example, if two
     * schemas "#alice" and "#bob" both have an "allOf" property that refers to the other, a naive
     * validator might get stuck in an infinite recursive loop trying to validate the instance. Schemas
     * SHOULD NOT make use of infinite recursive nesting like this; the behavior is undefined.
     */
    $REF(keywordMetadata().expects(STRING)),

    /**
     * From draft-06
     * <p>
     * The "$id" keyword defines a URI for the schema, and the base URI that other URI references
     * within the schema are resolved against. The "$id" keyword itself is resolved against the base
     * URI that the object as a whole appears in.
     * <p>
     * If present, the value for this keyword MUST be a string, and MUST represent a valid URI-reference
     * [RFC3986]. This value SHOULD be normalized, and SHOULD NOT be an empty fragment <#> or an empty string <>.
     * <p>
     * The root schema of a JSON Schema document SHOULD contain an "$id" keyword with a URI (containing
     * a scheme). This URI SHOULD either not have a fragment, or have one that is an empty string. [CREF2]
     * <p>
     * To name subschemas in a JSON Schema document, subschemas can use "$id" to give themselves a
     * document-local identifier. This is done by setting "$id" to a URI reference consisting only of
     * a fragment. The fragment identifier MUST begin with a letter ([A-Za-z]), followed by any number of
     * letters, digits ([0-9]), hyphens ("-"), underscores ("_"), colons (":"), or periods (".").
     * <p>
     * The effect of defining an "$id" that neither matches the above requirements nor is a valid JSON pointer
     * is not defined.
     */
    $ID(keywordMetadata().expects(STRING).since(Draft6)),

    /**
     * From draft-03
     * <p>
     * This attribute defines the current URI of this schema (this attribute
     * is effectively a "self" link).  This URI MAY be relative or absolute.
     * If the URI is relative it is resolved against the current URI of the
     * parent schema it is contained in.  If this schema is not contained in
     * any parent schema, the current URI of the parent schema is held to be
     * the URI under which this schema was addressed.  If id is missing, the
     * current URI of a schema is defined to be that of the parent schema.
     * The current URI of the schema is also used to construct relative
     * references such as for $ref.
     */
    ID(keywordMetadata().expects(STRING).since(Draft3).until(Draft5)),

    /**
     * From draft-06
     * <p>
     * The value of both of these keywords MUST be a string.
     * <p>
     * Both of these keywords can be used to decorate a user interface with information about the data
     * produced by this user interface. A title will preferably be short, whereas a description will
     * provide explanation about the purpose of the instance described by this schema.
     */
    TITLE(keywordMetadata().expects(STRING)),

    /**
     * From draft-06
     * <p>
     * This keyword's value MUST be an object. Each member value of this object MUST be a valid JSON Schema.
     * <p>
     * This keyword plays no role in validation per se. Its role is to provide a standardized location for
     * schema authors to inline JSON Schemas into a more general schema.
     * <p>
     * <p>
     * {
     * "type": "array",
     * "items": { "$ref": "#/definitions/positiveInteger" },
     * "definitions": {
     * "positiveInteger": {
     * "type": "integer",
     * "exclusiveMinimum": 0
     * }
     * }
     * }
     * <p>
     * <p>
     * As an example, here is a schema describing an array of positive integers, where the positive integer
     * constraint is a subschema in "definitions":
     */
    DEFINITIONS(keywordMetadata().expects(OBJECT).since(Draft6)),

    /**
     * From draft-06
     * <p>
     * The value of both of these keywords (title, description) MUST be a string.
     * <p>
     * Both of these keywords can be used to decorate a user interface with information about the data
     * produced by this user interface. A title will preferably be short, whereas a description will
     * provide explanation about the purpose of the instance described by this schema.
     */
    DESCRIPTION(keywordMetadata().expects(STRING)),

    /**
     * From draft-06
     * <p>
     * There are no restrictions placed on the value of this keyword.
     * <p>
     * This keyword can be used to supply a default JSON value associated with a
     * particular schema. It is RECOMMENDED that a default value be valid against
     * the associated schema.
     */
    DEFAULT(keywordMetadata().expects(ARRAY, OBJECT, NUMBER, TRUE, FALSE, STRING, NULL)),

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
    PROPERTIES(keywordMetadata().expects(OBJECT).validates(JsonSchemaType.OBJECT)),

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An object instance is valid against "maxProperties" if its
     * number of properties is less than, or equal to, the value of this
     * keyword.
     */
    MAX_PROPERTIES(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.OBJECT).since(Draft4)),

    /**
     * The value of this keyword MUST be an array.
     * Elements of this array, if any, MUST be strings, and MUST be unique.
     * <p>
     * An object instance is valid against this keyword if every item in the array is
     * the name of a property in the instance.
     * <p>
     * Omitting this keyword has the same behavior as an empty array.
     */
    REQUIRED(keywordMetadata().expects(ARRAY).validates(JsonSchemaType.OBJECT).since(Draft4)),

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
    ADDITIONAL_PROPERTIES(keywordMetadata().expects(OBJECT).validates(JsonSchemaType.OBJECT).since(Draft6)),

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An object instance is valid against "minProperties" if its
     * number of properties is greater than, or equal to, the value of this
     * keyword.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    MIN_PROPERTIES(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.OBJECT).since(Draft4)),

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
    DEPENDENCIES(keywordMetadata().expects(OBJECT).validates(JsonSchemaType.OBJECT)),
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
    PATTERN_PROPERTIES(keywordMetadata().expects(OBJECT).validates(JsonSchemaType.OBJECT)),
    /**
     * The value of "propertyNames" MUST be a valid JSON Schema.
     * <p>
     * If the instance is an object, this keyword validates if every property name in
     * the instance validates against the provided schema.
     * Note the property name that the schema is testing will always be a string.
     * <p>
     * Omitting this keyword has the same behavior as an empty schema.
     */
    PROPERTY_NAMES(keywordMetadata().expects(ARRAY).validates(JsonSchemaType.OBJECT).since(Draft6)),

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
    TYPE(keywordMetadata().expects(STRING).since(Draft4)),

    /**
     * The value of "multipleOf" MUST be a number, strictly greater than 0.
     * <p>
     * A numeric instance is valid only if division by this keyword's value results in
     * an integer.
     */
    MULTIPLE_OF(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.NUMBER).since(Draft4)),

    /**
     * The value of "maximum" MUST be a number, representing an inclusive upper limit
     * for a numeric instance.
     * <p>
     * If the instance is a number, then this keyword validates only if the instance is
     * less than or exactly equal to "maximum".
     */
    MAXIMUM(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.NUMBER)),

    /**
     * The value of "exclusiveMaximum" MUST be number, representing an exclusive upper
     * limit for a numeric instance.
     * <p>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly less than (not equal to) "exclusiveMaximum".
     */
    EXCLUSIVE_MAXIMUM(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.NUMBER).since(Draft6)),

    /**
     * The value of "minimum" MUST be a number, representing an inclusive lower limit
     * for a numeric instance.
     * <p>
     * If the instance is a number, then this keyword validates only if the instance is
     * greater than or exactly equal to "minimum".
     */
    MINIMUM(keywordMetadata().validates(JsonSchemaType.NUMBER).expects(NUMBER)),

    /**
     * The value of "exclusiveMinimum" MUST be number, representing an exclusive lower
     * limit for a numeric instance.
     * <p>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly greater than (not equal to) "exclusiveMinimum".
     */
    EXCLUSIVE_MINIMUM(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.NUMBER).since(Draft6)),

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
    FORMAT(keywordMetadata().expects(STRING).validates(JsonSchemaType.STRING)),

    /**
     * The value of this keyword MUST be a non-negative integer.
     * A string instance is valid against this keyword if its
     * length is less than, or equal to, the value of this keyword.
     * <p>
     * The length of a string instance is defined as the number of its
     * characters as defined by <a href="https://tools.ietf.org/html/RFC7159">RFC 7159</a>.
     */
    MAX_LENGTH(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.STRING)),

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
    MIN_LENGTH(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.STRING)),

    /**
     * The value of this keyword MUST be a string. This string SHOULD be a
     * valid regular expression, according to the ECMA 262 regular expression
     * dialect.
     * <p>
     * A string instance is considered valid if the regular
     * expression matches the instance successfully. Recall: regular
     * expressions are not implicitly anchored.
     */
    PATTERN(keywordMetadata().expects(STRING).validates(JsonSchemaType.STRING)),

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
    ITEMS(keywordMetadata().expects(ARRAY, OBJECT).validates(JsonSchemaType.ARRAY)),

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
    ADDITIONAL_ITEMS(keywordMetadata().expects(OBJECT).validates(JsonSchemaType.ARRAY).since(Draft6)),

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An array instance is valid against "maxItems" if its size is
     * less than, or equal to, the value of this keyword.
     */
    MAX_ITEMS(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.ARRAY)),

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An array instance is valid against "minItems" if its size is
     * greater than, or equal to, the value of this keyword.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    MIN_ITEMS(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.ARRAY)),

    /**
     * The value of this keyword MUST be a boolean.
     * <p>
     * If this keyword has boolean value false, the instance validates
     * successfully. If it has boolean value true, the instance validates
     * successfully if all of its elements are unique.
     * <p>
     * Omitting this keyword has the same behavior as a value of false.
     */
    UNIQUE_ITEMS(keywordMetadata().expects(TRUE, FALSE).validates(JsonSchemaType.ARRAY)),

    /**
     * The value of this keyword MUST be a valid JSON Schema.
     * <p>
     * An array instance is valid against "contains" if at least one of
     * its elements is valid against the given schema.
     */
    CONTAINS(keywordMetadata().expects(OBJECT).validates(JsonSchemaType.ARRAY).since(Draft6)),

    /**
     * The value of this keyword MUST be an array. This array SHOULD have at
     * least one element. Elements in the array SHOULD be unique.
     * <p>
     * An instance validates successfully against this keyword if its value is
     * equal to one of the elements in this keyword's array value.
     * <p>
     * Elements in the array might be of any value, including null.
     */
    ENUM(keywordMetadata().expects(ARRAY)),

    /**
     * The value of this keyword MUST be an array. There are no restrictions placed on the values within the array.
     * <p>
     * This keyword can be used to provide sample JSON values associated with a particular schema, for the purpose
     * of illustrating usage. It is RECOMMENDED that these values be valid against the associated schema.
     * <p>
     * Implementations MAY use the value of "default", if present, as an additional example. If "examples" is
     * absent, "default" MAY still be used in this manner.
     */
    EXAMPLES(keywordMetadata().expects(ARRAY).since(Draft6)),

    /**
     * The value of this keyword MAY be of any type, including null.
     * <p>
     * An instance validates successfully against this keyword if its value is
     * equal to the value of the keyword.
     */
    CONST(keywordMetadata().expects(OBJECT, ARRAY, STRING, NUMBER, TRUE, FALSE, NULL).forAllSchemas().since(Draft6)),

    /**
     * This keyword's value MUST be a valid JSON Schema.
     * <p>
     * An instance is valid against this keyword if it fails to validate
     * successfully against the schema defined by this keyword.
     */
    NOT(keywordMetadata().expects(OBJECT).validatesAnyType().since(Draft4)),
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against all schemas defined by this keyword's value.
     */
    ALL_OF(keywordMetadata().expects(ARRAY).validatesAnyType().since(Draft4)),
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against at least one schema defined by this keyword's value.
     */
    ANY_OF(keywordMetadata().expects(ARRAY).validatesAnyType().since(Draft4)),
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against exactly one schema defined by this keyword's value.
     */
    ONE_OF(keywordMetadata().expects(ARRAY).validatesAnyType().since(Draft4)),

    /**
     * From draft-3 schema
     * <p>
     * This attribute defines what the primitive type or the schema of the
     * instance MUST be in order to validate.  This attribute can take one
     * of two forms:
     * <p>
     * Simple Types  A string indicating a primitive or simple type.  The
     * following are acceptable string values:
     * <p>
     * string  Value MUST be a string.
     * <p>
     * number  Value MUST be a number, floating point numbers are
     * allowed.
     * <p>
     * integer  Value MUST be an integer, no floating point numbers are
     * allowed.  This is a subset of the number type.
     * <p>
     * boolean  Value MUST be a boolean.
     * <p>
     * object  Value MUST be an object.
     * <p>
     * array  Value MUST be an array.
     * <p>
     * null  Value MUST be null.  Note this is mainly for purpose of
     * being able use union types to define nullability.  If this type
     * is not included in a union, null values are not allowed (the
     * primitives listed above do not allow nulls on their own).
     * <p>
     * any  Value MAY be of any type including null.
     * <p>
     * If the property is not defined or is not in this list, then any
     * type of value is acceptable.  Other type values MAY be used for
     * custom purposes, but minimal validators of the specification
     * implementation can allow any instance value on unknown type
     * values.
     * <p>
     * Union Types  An array of two or more simple type definitions.  Each
     * item in the array MUST be a simple type definition or a schema.
     * The instance value is valid if it is of the same type as one of
     * the simple type definitions, or valid by one of the schemas, in
     * the array.
     * <p>
     * For example, a schema that defines if an instance can be a string or
     * a number would be:
     * <p>
     * {"type":["string","number"]}
     */
    TYPE_WITH_ANY(keywordMetadata().key("type").expects(ARRAY, STRING).validatesAnyType().since(Draft3).until(Draft3)),

    /**
     * From draft-3 schema
     * <p>
     * This provides a definition for additional items in an array instance
     * when tuple definitions of the items is provided.  This can be false
     * to indicate additional items in the array are not allowed, or it can
     * be a schema that defines the schema of the additional items.
     */
    ADDITIONAL_PROPERTIES_WITH_BOOLEAN(keywordMetadata().key("additionalProperties").expects(OBJECT, TRUE, FALSE)
            .validates(JsonSchemaType.OBJECT)
            .from(Draft3)
            .until(Draft5)),

    /**
     * From draft-3 schema
     * <p>
     * This provides a definition for additional items in an array instance
     * when tuple definitions of the items is provided.  This can be false
     * to indicate additional items in the array are not allowed, or it can
     * be a schema that defines the schema of the additional items.
     */
    ADDITIONAL_ITEMS_WITH_BOOLEAN(keywordMetadata().key("additionalItems").expects(OBJECT, TRUE, FALSE)
            .validates(JsonSchemaType.ARRAY)
            .from(Draft3)
            .until(Draft5)),

    /**
     * From draft-3 schema
     * <p>
     * This attribute indicates if the instance must have a value, and not
     * be undefined.  This is false by default, making the instance
     * optional.
     */
    REQUIRED_PROPERTY(keywordMetadata().key("required").expects(TRUE, FALSE).validatesAnyType().from(Draft3).until(Draft3)),

    /**
     * From draft-03
     * <p>
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "maximum" attribute.  This is false by default, meaning the instance
     * value can be less then or equal to the maximum value.
     */
    EXCLUSIVE_MAXIMUM_BOOLEAN(keywordMetadata().key("exclusiveMaximum").expects(TRUE, FALSE).validatesAnyType().from(Draft3).until(Draft5)),

    /**
     * From draft-03
     * <p>
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "minimum" attribute.  This is false by default, meaning the instance
     * value can be greater then or equal to the minimum value.
     */
    EXCLUSIVE_MINIMUM_BOOLEAN(keywordMetadata().key("exclusiveMinimum").expects(TRUE, FALSE).validatesAnyType().from(Draft3).until(Draft5)),

    /**
     * From draft-03
     * <p>
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "minimum" attribute.  This is false by default, meaning the instance
     * value can be greater then or equal to the minimum value.
     */
    DIVISIBLE_BY(keywordMetadata().expects(NUMBER).validates(JsonSchemaType.NUMBER).from(Draft3).since(Draft3)),

    /**
     * From draft-03
     * <p>
     * This attribute takes the same values as the "type" attribute, however
     * if the instance matches the type or if this value is an array and the
     * instance matches any type or schema in the array, then this instance
     * is not valid.
     */
    DISALLOW(keywordMetadata().expects(STRING, ARRAY).validatesAnyType().from(Draft3).until(Draft3)),

    /**
     * From draft-03
     * <p>
     * The value of this property MUST be another schema which will provide
     * a base schema which the current schema will inherit from.  The
     * inheritance rules are such that any instance that is valid according
     * to the current schema MUST be valid according to the referenced
     * schema.  This MAY also be an array, in which case, the instance MUST
     * be valid for all the schemas in the array.  A schema that extends
     * another schema MAY define additional attributes, constrain existing
     * attributes, or add other constraints.
     * <p>
     * Conceptually, the behavior of extends can be seen as validating an
     * instance against all constraints in the extending schema as well as
     * the extended schema(s).  More optimized implementations that merge
     * schemas are possible, but are not required.  An example of using
     * "extends":
     * <p>
     * {
     * "description":"An adult",
     * "properties":{"age":{"minimum": 21}},
     * "extends":"person"
     * }
     * <p>
     * {
     * "description":"Extended schema",
     * "properties":{"deprecated":{"type": "boolean"}},
     * "extends":"http://json-schema.org/draft-03/schema"
     * }
     */
    EXTENDS(keywordMetadata().expects(OBJECT).validatesAnyType().from(Draft3).until(Draft3));

    @Delegate
    private final KeywordMetadata keywordMetadata;

    JsonSchemaKeywordType(KeywordMetadataBuilder metadata) {
        this.keywordMetadata = metadata.name(this.name()).build();
    }

    public String key() {
        return keywordMetadata.getKey();
    }

    public String toString() {
        return keywordMetadata.getKey();
    }

}
