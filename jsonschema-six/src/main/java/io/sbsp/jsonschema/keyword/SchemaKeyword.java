package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$ID;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$REF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$SCHEMA;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ALL_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ANY_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONST;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONTAINS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DEFAULT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DEPENDENCIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DESCRIPTION;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ENUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.EXAMPLES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.EXCLUSIVE_MAXIMUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.EXCLUSIVE_MINIMUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.EXTENDS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.FORMAT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ID;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAXIMUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_LENGTH;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MINIMUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_LENGTH;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MULTIPLE_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.NOT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ONE_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PATTERN;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PATTERN_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PROPERTY_NAMES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.REQUIRED;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.REQUIRED_PROPERTY;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TITLE;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TYPE;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.UNIQUE_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft4;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft5;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft6;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.booleanKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.jsonArrayKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.jsonValueKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.keyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.numberKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.schemaListKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.schemaMapKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.singleSchemaKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.stringKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.stringSetKeyword;
import static io.sbsp.jsonschema.keyword.KeywordMetadata.uriKeyword;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NULL;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;

public interface SchemaKeyword {

    void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version);

    KeywordMetadata<URIKeyword> $schema = uriKeyword($SCHEMA).expects(STRING).build();
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
    KeywordMetadata<URIKeyword> $ref = uriKeyword($REF).expects(STRING).build();

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
     * KeywordMetadata * a fragment. The fragment identifier MUST begin with a letter  = [A-Za-z].build(); followed by any number of
     * KeywordMetadata * letters, digits  = [0-9].build(); hyphens ("-"), underscores ("_"), colons (":"), or periods (".").
     * <p>
     * The effect of defining an "$id" that neither matches the above requirements nor is a valid JSON pointer
     * is not defined.
     */
    KeywordMetadata<URIKeyword> $id = uriKeyword($ID).expects(STRING).since(Draft6).build();

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
    KeywordMetadata<URIKeyword> id = uriKeyword(ID).expects(STRING).since(Draft3).until(Draft5).build();

    /**
     * From draft-06
     * <p>
     * The value of both of these keywords MUST be a string.
     * <p>
     * Both of these keywords can be used to decorate a user interface with information about the data
     * produced by this user interface. A title will preferably be short, whereas a description will
     * provide explanation about the purpose of the instance described by this schema.
     */
    KeywordMetadata<StringKeyword> title = stringKeyword(TITLE).expects(STRING).build();

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
    KeywordMetadata<SchemaMapKeyword> definitions = schemaMapKeyword(DESCRIPTION).expects(OBJECT).since(Draft6).build();

    /**
     * From draft-06
     * <p>
     * The value of both of these keywords (title, description) MUST be a string.
     * <p>
     * Both of these keywords can be used to decorate a user interface with information about the data
     * produced by this user interface. A title will preferably be short, whereas a description will
     * provide explanation about the purpose of the instance described by this schema.
     */
    KeywordMetadata<StringKeyword> description = stringKeyword(DESCRIPTION).expects(STRING).build();

    /**
     * From draft-06
     * <p>
     * There are no restrictions placed on the value of this keyword.
     * <p>
     * This keyword can be used to supply a default JSON value associated with a
     * particular schema. It is RECOMMENDED that a default value be valid against
     * the associated schema.
     */
    KeywordMetadata<JsonValueKeyword> $default = jsonValueKeyword(DEFAULT).expects(ARRAY, OBJECT, NUMBER, TRUE, FALSE, STRING, NULL).build();

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
    KeywordMetadata<SchemaMapKeyword> properties = schemaMapKeyword(PROPERTIES).expects(OBJECT).validates(JsonSchemaType.OBJECT).build();

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An object instance is valid against "maxProperties" if its
     * number of properties is less than, or equal to, the value of this
     * keyword.
     */
    KeywordMetadata<NumberKeyword> maxProperties = numberKeyword(MAX_PROPERTIES).expects(NUMBER).validates(JsonSchemaType.OBJECT).since(Draft4).build();

    /**
     * The value of this keyword MUST be an array.
     * Elements of this array, if any, MUST be strings, and MUST be unique.
     * <p>
     * An object instance is valid against this keyword if every item in the array is
     * the name of a property in the instance.
     * <p>
     * Omitting this keyword has the same behavior as an empty array.
     */
    KeywordMetadata<StringSetKeyword> required = stringSetKeyword(REQUIRED).expects(ARRAY).validates(JsonSchemaType.OBJECT).since(Draft4).build();

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
    KeywordMetadata<SingleSchemaKeyword> additionalProperties = singleSchemaKeyword(ADDITIONAL_PROPERTIES).expects(OBJECT).validates(JsonSchemaType.OBJECT).since(Draft6).build();

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An object instance is valid against "minProperties" if its
     * number of properties is greater than, or equal to, the value of this
     * keyword.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    KeywordMetadata<NumberKeyword> minProperties = numberKeyword(MIN_PROPERTIES).expects(NUMBER).validates(JsonSchemaType.OBJECT).since(Draft4).build();

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
    KeywordMetadata<PropertyDependencyKeyword> dependencies = keyword(PropertyDependencyKeyword.class).key(DEPENDENCIES).expects(OBJECT).validates(JsonSchemaType.OBJECT).build();

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
    KeywordMetadata<SchemaMapKeyword> patternProperties = schemaMapKeyword(PATTERN_PROPERTIES).expects(OBJECT).validates(JsonSchemaType.OBJECT).build();
    /**
     * The value of "propertyNames" MUST be a valid JSON Schema.
     * <p>
     * If the instance is an object, this keyword validates if every property name in
     * the instance validates against the provided schema.
     * Note the property name that the schema is testing will always be a string.
     * <p>
     * Omitting this keyword has the same behavior as an empty schema.
     */
    KeywordMetadata<SingleSchemaKeyword> propertyNames = singleSchemaKeyword(PROPERTY_NAMES).expects(ARRAY).validates(JsonSchemaType.OBJECT).since(Draft6).build();

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
    KeywordMetadata<TypeKeyword> type = keyword(TypeKeyword.class).key(TYPE).expects(STRING, ARRAY).since(Draft4).build();

    /**
     * The value of "multipleOf" MUST be a number, strictly greater than 0.
     * <p>
     * A numeric instance is valid only if division by this keyword's value results in
     * an integer.
     */
    KeywordMetadata<NumberKeyword> multipleOf = numberKeyword(MULTIPLE_OF).expects(NUMBER).validates(JsonSchemaType.NUMBER).since(Draft4).build();

    /**
     * The value of "maximum" MUST be a number, representing an inclusive upper limit
     * for a numeric instance.
     * <p>
     * If the instance is a number, then this keyword validates only if the instance is
     * less than or exactly equal to "maximum".
     */
    KeywordMetadata<MaximumKeyword> maximum = keyword(MaximumKeyword.class).key(MAXIMUM).expects(NUMBER).validates(JsonSchemaType.NUMBER).build();

    /**
     * The value of "exclusiveMaximum" MUST be number, representing an exclusive upper
     * limit for a numeric instance.
     * <p>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly less than (not equal to) "exclusiveMaximum".
     */
    KeywordMetadata<MaximumKeyword> exclusiveMaximum = keyword(MaximumKeyword.class).key(EXCLUSIVE_MAXIMUM).expects(NUMBER).validates(JsonSchemaType.NUMBER).since(Draft6).build();

    /**
     * The value of "minimum" MUST be a number, representing an inclusive lower limit
     * for a numeric instance.
     * <p>
     * If the instance is a number, then this keyword validates only if the instance is
     * greater than or exactly equal to "minimum".
     */
    KeywordMetadata<MinimumKeyword> minimum = keyword(MinimumKeyword.class).key(MINIMUM).validates(JsonSchemaType.NUMBER).expects(NUMBER).build();

    /**
     * The value of "exclusiveMinimum" MUST be number, representing an exclusive lower
     * limit for a numeric instance.
     * <p>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly greater than (not equal to) "exclusiveMinimum".
     */
    KeywordMetadata<MinimumKeyword> exclusiveMinimum = keyword(MinimumKeyword.class).key(EXCLUSIVE_MINIMUM).expects(NUMBER).validates(JsonSchemaType.NUMBER).since(Draft6).build();

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
    KeywordMetadata<StringKeyword> format = stringKeyword(FORMAT).expects(STRING).validates(JsonSchemaType.STRING).build();

    /**
     * The value of this keyword MUST be a non-negative integer.
     * A string instance is valid against this keyword if its
     * length is less than, or equal to, the value of this keyword.
     * <p>
     * The length of a string instance is defined as the number of its
     * characters as defined by <a href="https://tools.ietf.org/html/RFC7159">RFC 7159</a>.
     */
    KeywordMetadata<NumberKeyword> maxLength = numberKeyword(MAX_LENGTH).expects(NUMBER).validates(JsonSchemaType.STRING).build();

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
    KeywordMetadata<NumberKeyword> minLength = numberKeyword(MIN_LENGTH).expects(NUMBER).validates(JsonSchemaType.STRING).build();

    /**
     * The value of this keyword MUST be a string. This string SHOULD be a
     * valid regular expression, according to the ECMA 262 regular expression
     * dialect.
     * <p>
     * A string instance is considered valid if the regular
     * expression matches the instance successfully. Recall: regular
     * expressions are not implicitly anchored.
     */
    KeywordMetadata<StringKeyword> pattern = stringKeyword(PATTERN).expects(STRING).validates(JsonSchemaType.STRING).build();

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
    KeywordMetadata<ItemsKeyword> items = keyword(ItemsKeyword.class).key(ITEMS).expects(ARRAY, OBJECT).validates(JsonSchemaType.ARRAY).build();

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
    KeywordMetadata<SingleSchemaKeyword> additionalItems = singleSchemaKeyword(ADDITIONAL_PROPERTIES).expects(OBJECT).validates(JsonSchemaType.ARRAY).since(Draft6).build();

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An array instance is valid against "maxItems" if its size is
     * less than, or equal to, the value of this keyword.
     */
    KeywordMetadata<NumberKeyword> maxItems = numberKeyword(MAX_ITEMS).expects(NUMBER).validates(JsonSchemaType.ARRAY).build();

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An array instance is valid against "minItems" if its size is
     * greater than, or equal to, the value of this keyword.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    KeywordMetadata<NumberKeyword> minItems = numberKeyword(MIN_ITEMS).expects(NUMBER).validates(JsonSchemaType.ARRAY).build();

    /**
     * The value of this keyword MUST be a boolean.
     * <p>
     * If this keyword has boolean value false, the instance validates
     * successfully. If it has boolean value true, the instance validates
     * successfully if all of its elements are unique.
     * <p>
     * Omitting this keyword has the same behavior as a value of false.
     */
    KeywordMetadata<BooleanKeyword> uniqueItems = booleanKeyword(UNIQUE_ITEMS).expects(TRUE, FALSE).validates(JsonSchemaType.ARRAY).build();

    /**
     * The value of this keyword MUST be a valid JSON Schema.
     * <p>
     * An array instance is valid against "contains" if at least one of
     * its elements is valid against the given schema.
     */
    KeywordMetadata<SingleSchemaKeyword> contains = singleSchemaKeyword(CONTAINS).expects(OBJECT).validates(JsonSchemaType.ARRAY).since(Draft6).build();

    /**
     * The value of this keyword MUST be an array. This array SHOULD have at
     * least one element. Elements in the array SHOULD be unique.
     * <p>
     * An instance validates successfully against this keyword if its value is
     * equal to one of the elements in this keyword's array value.
     * <p>
     * Elements in the array might be of any value, including null.
     */
    KeywordMetadata<JsonArrayKeyword> $enum = jsonArrayKeyword(ENUM).expects(ARRAY).build();

    /**
     * The value of this keyword MUST be an array. There are no restrictions placed on the values within the array.
     * <p>
     * This keyword can be used to provide sample JSON values associated with a particular schema, for the purpose
     * of illustrating usage. It is RECOMMENDED that these values be valid against the associated schema.
     * <p>
     * Implementations MAY use the value of "default", if present, as an additional example. If "examples" is
     * absent, "default" MAY still be used in this manner.
     */
    KeywordMetadata<JsonArrayKeyword> examples = jsonArrayKeyword(EXAMPLES).expects(ARRAY).since(Draft6).build();

    /**
     * The value of this keyword MAY be of any type, including null.
     * <p>
     * An instance validates successfully against this keyword if its value is
     * equal to the value of the keyword.
     */
    KeywordMetadata<JsonValueKeyword> $const = jsonValueKeyword(CONST).expects(OBJECT, ARRAY, STRING, NUMBER, TRUE, FALSE, NULL).forAllSchemas().since(Draft6).build();

    /**
     * This keyword's value MUST be a valid JSON Schema.
     * <p>
     * An instance is valid against this keyword if it fails to validate
     * successfully against the schema defined by this keyword.
     */
    KeywordMetadata<SingleSchemaKeyword> not = singleSchemaKeyword(NOT).expects(OBJECT).validatesAnyType().since(Draft4).build();
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against all schemas defined by this keyword's value.
     */
    KeywordMetadata<SchemaListKeyword> allOf = schemaListKeyword(ALL_OF).expects(ARRAY).validatesAnyType().since(Draft4).build();
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against at least one schema defined by this keyword's value.
     */
    KeywordMetadata<SchemaListKeyword> anyOf = schemaListKeyword(ANY_OF).expects(ARRAY).validatesAnyType().since(Draft4).build();
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against exactly one schema defined by this keyword's value.
     */
    KeywordMetadata<SchemaListKeyword> oneOf = schemaListKeyword(ONE_OF).expects(ARRAY).validatesAnyType().since(Draft4).build();

    // /**
    //  * From draft-3 schema
    //  * <p>
    //  * This attribute defines what the primitive type or the schema of the
    //  * instance MUST be in order to validate.  This attribute can take one
    //  * of two forms:
    //  * <p>
    //  * Simple Types  A string indicating a primitive or simple type.  The
    //  * following are acceptable string values:
    //  * <p>
    //  * string  Value MUST be a string.
    //  * <p>
    //  * number  Value MUST be a number, floating point numbers are
    //  * allowed.
    //  * <p>
    //  * integer  Value MUST be an integer, no floating point numbers are
    //  * allowed.  This is a subset of the number type.
    //  * <p>
    //  * boolean  Value MUST be a boolean.
    //  * <p>
    //  * object  Value MUST be an object.
    //  * <p>
    //  * array  Value MUST be an array.
    //  * <p>
    //  * null  Value MUST be null.  Note this is mainly for purpose of
    //  * being able use union types to define nullability.  If this type
    //  * is not included in a union, null values are not allowed (the
    //  * primitives listed above do not allow nulls on their own).
    //  * <p>
    //  * any  Value MAY be of any type including null.
    //  * <p>
    //  * If the property is not defined or is not in this list, then any
    //  * type of value is acceptable.  Other type values MAY be used for
    //  * custom purposes, but minimal validators of the specification
    //  * implementation can allow any instance value on unknown type
    //  * values.
    //  * <p>
    //  * Union Types  An array of two or more simple type definitions.  Each
    //  * item in the array MUST be a simple type definition or a schema.
    //  * The instance value is valid if it is of the same type as one of
    //  * the simple type definitions, or valid by one of the schemas, in
    //  * the array.
    //  * <p>
    //  * For example, a schema that defines if an instance can be a string or
    //  * a number would be:
    //  * <p>
    //  * {"type":["string","number"]}
    //  */
    //  KeywordMetadata<StringSetKeyword> typeDraft3 = stringSetKeyword(TYPE_WITH_ANY).expects(ARRAY, STRING).validatesAnyType().since(Draft3).until(Draft3).build();
    //
    // /**
    //  * From draft-3 schema
    //  * <p>
    //  * This provides a definition for additional items in an array instance
    //  * when tuple definitions of the items is provided.  This can be false
    //  * to indicate additional items in the array are not allowed, or it can
    //  * be a schema that defines the schema of the additional items.
    //  */
    // KeywordMADDITIONAL_PROPERTIES_WITH_BOOLEAN(keywordMetadata().key("additionalProperties").expects(OBJECT, TRUE, FALSE)
    //         .validates(JsonSchemaType.OBJECT)
    //         .from(Draft3)
    //          until = Draft5).build();
    //
    // /**
    //  * From draft-3 schema
    //  * <p>
    //  * This provides a definition for additional items in an array instance
    //  * when tuple definitions of the items is provided.  This can be false
    //  * to indicate additional items in the array are not allowed, or it can
    //  * be a schema that defines the schema of the additional items.
    //  */
    // ADDITIONAL_ITEMS_WITH_BOOLEAN(keywordMetadata().key("additionalItems").expects(OBJECT, TRUE, FALSE)
    //         .validates(JsonSchemaType.ARRAY)
    //         .from(Draft3)
    //          until = Draft5).build();
    //
    /**
     * From draft-3 schema
     * <p>
     * This attribute indicates if the instance must have a value, and not
     * be undefined.  This is false by default, making the instance
     * optional.
     */
     KeywordMetadata<BooleanKeyword> requiredDraft3 = KeywordMetadata.booleanKeyword(REQUIRED_PROPERTY).expects(TRUE, FALSE).validatesAnyType().from(Draft3).until(Draft3).build();

    // /**
    //  * From draft-03
    //  * <p>
    //  * This attribute indicates if the value of the instance (if the
    //  * instance is a number) can not equal the number defined by the
    //  * "maximum" attribute.  This is false by default, meaning the instance
    //  * value can be less then or equal to the maximum value.
    //  */
    //  KeywordMetadata EXCLUSIVE_MAXIMUM_boolean =keywordMetadata().key("exclusiveMaximum").expects(TRUE, FALSE).validatesAnyType().from(Draft3).until(Draft5).build();
    //
    // /**
    //  * From draft-03
    //  * <p>
    //  * This attribute indicates if the value of the instance (if the
    //  * instance is a number) can not equal the number defined by the
    //  * "minimum" attribute.  This is false by default, meaning the instance
    //  * value can be greater then or equal to the minimum value.
    //  */
    //  KeywordMetadata EXCLUSIVE_MINIMUM_boolean =keywordMetadata().key("exclusiveMinimum").expects(TRUE, FALSE).validatesAnyType().from(Draft3).until(Draft5).build();
    //
    // /**
    //  * From draft-03
    //  * <p>
    //  * This attribute indicates if the value of the instance (if the
    //  * instance is a number) can not equal the number defined by the
    //  * "minimum" attribute.  This is false by default, meaning the instance
    //  * value can be greater then or equal to the minimum value.
    //  */
    //  KeywordMetadata<SimpleKeyword<Number>> DIVISIBLE_by =simpleKeyword(Number.class).expects(NUMBER).validates(JsonSchemaType.NUMBER).from(Draft3).since(Draft3).build();
    //
    // /**
    //  * From draft-03
    //  * <p>
    //  * This attribute takes the same values as the "type" attribute, however
    //  * if the instance matches the type or if this value is an array and the
    //  * instance matches any type or schema in the array, then this instance
    //  * is not valid.
    //  */
    //  KeywordMetadata disallow =keywordMetadata().expects(STRING, ARRAY).validatesAnyType().from(Draft3).until(Draft3).build();
    //
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
    KeywordMetadata<SingleSchemaKeyword> $extends = singleSchemaKeyword(EXTENDS).expects(OBJECT).validatesAnyType().from(Draft3).until(Draft3).build();

}
