package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaType;

import static io.sbsp.jsonschema.enums.JsonSchemaType.INTEGER;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft4;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft5;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft6;
import static io.sbsp.jsonschema.keyword.KeywordInfo.KeywordInfoBuilder;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NULL;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;

public interface Keywords {

    String $ID_KEY = "$id";
    String ID_KEY = "id";

    KeywordInfo<URIKeyword> $SCHEMA = uriKeyword("$schema").expects(STRING).build();
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
     * validation might get stuck in an infinite recursive loop trying to validate the instance. Schemas
     * SHOULD NOT make use of infinite recursive nesting like this; the behavior is undefined.
     */
    KeywordInfo<URIKeyword> $REF = uriKeyword("$ref").expects(STRING).build();
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
    KeywordInfo<URIKeyword> $ID = uriKeyword("$id").expects(STRING).since(Draft6).build();

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
    KeywordInfo<URIKeyword> ID = uriKeyword("id").expects(STRING).since(Draft6).build();

    /**
     * From draft-06
     * <p>
     * The value of both of these keywords MUST be a string.
     * <p>
     * Both of these keywords can be used to decorate a user interface with information about the data
     * produced by this user interface. A title will preferably be short, whereas a description will
     * provide explanation about the purpose of the instance described by this schema.
     */
    KeywordInfo<StringKeyword> TITLE = stringKeyword("title").expects(STRING).build();

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
    KeywordInfo<SchemaMapKeyword> DEFINITIONS = schemaMapKeyword("definitions").expects(OBJECT).since(Draft6).build();
    /**
     * From draft-06
     * <p>
     * The value of both of these keywords (title, description) MUST be a string.
     * <p>
     * Both of these keywords can be used to decorate a user interface with information about the data
     * produced by this user interface. A title will preferably be short, whereas a description will
     * provide explanation about the purpose of the instance described by this schema.
     */
    KeywordInfo<StringKeyword> DESCRIPTION = stringKeyword("description").expects(STRING).build();
    /**
     * From draft-06
     * <p>
     * There are no restrictions placed on the value of this keyword.
     * <p>
     * This keyword can be used to supply a default JSON value associated with a
     * particular schema. It is RECOMMENDED that a default value be valid against
     * the associated schema.
     */
    KeywordInfo<JsonValueKeyword> DEFAULT = jsonValueKeyword("default").expects(ARRAY)
        .additionalDefinition().expects(OBJECT)
            .additionalDefinition().expects(NUMBER)
            .additionalDefinition().expects(TRUE)
            .additionalDefinition().expects(FALSE)
            .additionalDefinition().expects(STRING)
            .additionalDefinition().expects(NULL)
            .build();
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
    KeywordInfo<SchemaMapKeyword> PROPERTIES = schemaMapKeyword("properties").expects(OBJECT).validates(JsonSchemaType.OBJECT).build();
    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An object instance is valid against "maxProperties" if its
     * number of properties is less than, or equal to, the value of this
     * keyword.
     */
    KeywordInfo<NumberKeyword> MAX_PROPERTIES = numberKeyword("maxProperties").expects(NUMBER).validates(JsonSchemaType.OBJECT).since(Draft4).build();
    /**
     * The value of this keyword MUST be an array.
     * Elements of this array, if any, MUST be strings, and MUST be unique.
     * <p>
     * An object instance is valid against this keyword if every item in the array is
     * the name of a property in the instance.
     * <p>
     * Omitting this keyword has the same behavior as an empty array.
     */
    KeywordInfo<StringSetKeyword> REQUIRED = stringSetKeyword("required").expects(ARRAY).validates(JsonSchemaType.OBJECT).since(Draft4).build();
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
    KeywordInfo<SingleSchemaKeyword> ADDITIONAL_PROPERTIES = singleSchemaKeyword("additionalProperties").expects(OBJECT).validates(JsonSchemaType.OBJECT).since(Draft3)
            .additionalDefinition().expects(FALSE).from(Draft3).until(Draft5)
            .additionalDefinition().expects(TRUE).from(Draft3).until(Draft5)
            .build();

    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An object instance is valid against "minProperties" if its
     * number of properties is greater than, or equal to, the value of this
     * keyword.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    KeywordInfo<NumberKeyword> MIN_PROPERTIES = numberKeyword("minProperties").expects(NUMBER).validates(JsonSchemaType.OBJECT).since(Draft4).build();
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
    KeywordInfo<DependenciesKeyword> DEPENDENCIES = keyword(DependenciesKeyword.class).key("dependencies").expects(OBJECT).validates(JsonSchemaType.OBJECT).build();
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
    KeywordInfo<SchemaMapKeyword> PATTERN_PROPERTIES = schemaMapKeyword("patternProperties").expects(OBJECT).validates(JsonSchemaType.OBJECT).build();
    /**
     * The value of "propertyNames" MUST be a valid JSON Schema.
     * <p>
     * If the instance is an object, this keyword validates if every property name in
     * the instance validates against the provided schema.
     * Note the property name that the schema is testing will always be a string.
     * <p>
     * Omitting this keyword has the same behavior as an empty schema.
     */
    KeywordInfo<SingleSchemaKeyword> PROPERTY_NAMES = singleSchemaKeyword("propertyNames").expects(ARRAY).validates(JsonSchemaType.OBJECT).since(Draft6).build();
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
    //todo:ericm Handle "any" and "disallow"
    KeywordInfo<TypeKeyword> TYPE = keyword(TypeKeyword.class).key("type").expects(STRING)
            .additionalDefinition().expects(ARRAY)
            .build();
    /**
     * The value of "multipleOf" MUST be a number, strictly greater than 0.
     * <p>
     * A numeric instance is valid only if division by this keyword's value results in
     * an integer.
     */
    KeywordInfo<NumberKeyword> MULTIPLE_OF = numberKeyword("multipleOf").expects(NUMBER).validates(JsonSchemaType.NUMBER, INTEGER).since(Draft4).build();
    /**
     * The value of "maximum" MUST be a number, representing an inclusive upper limit
     * for a numeric instance.
     * <p>
     * If the instance is a number, then this keyword validates only if the instance is
     * less than or exactly equal to "maximum".
     */
    KeywordInfo<LimitKeyword> MAXIMUM = keyword(LimitKeyword.class).key("maximum").expects(NUMBER).validates(JsonSchemaType.NUMBER, JsonSchemaType.INTEGER).build();
    /**
     * The value of "exclusiveMaximum" MUST be number, representing an exclusive upper
     * limit for a numeric instance.
     * <p>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly less than (not equal to) "exclusiveMaximum".
     */
    KeywordInfo<LimitKeyword> EXCLUSIVE_MAXIMUM = keyword(LimitKeyword.class).key("exclusiveMaximum").expects(NUMBER).validates(JsonSchemaType.NUMBER, INTEGER).since(Draft6)
            .additionalDefinition().expects(TRUE).from(Draft3).until(Draft5)
            .additionalDefinition().expects(FALSE).from(Draft3).until(Draft5)
            .build();
    /**
     * The value of "minimum" MUST be a number, representing an inclusive lower limit
     * for a numeric instance.
     * <p>
     * If the instance is a number, then this keyword validates only if the instance is
     * greater than or exactly equal to "minimum".
     */
    KeywordInfo<LimitKeyword> MINIMUM = keyword(LimitKeyword.class).key("minimum").validates(JsonSchemaType.NUMBER, INTEGER).expects(NUMBER).build();
    /**
     * The value of "exclusiveMinimum" MUST be number, representing an exclusive lower
     * limit for a numeric instance.
     * <p>
     * If the instance is a number, then the instance is valid only if it has a value
     * strictly greater than (not equal to) "exclusiveMinimum".
     */
    KeywordInfo<LimitKeyword> EXCLUSIVE_MINIMUM = keyword(LimitKeyword.class).key("exclusiveMinimum").expects(NUMBER).validates(JsonSchemaType.NUMBER, JsonSchemaType.INTEGER).since(Draft6)
            .additionalDefinition().expects(TRUE).from(Draft3).until(Draft5)
            .additionalDefinition().expects(FALSE).from(Draft3).until(Draft5)
            .build();
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
    KeywordInfo<StringKeyword> FORMAT = stringKeyword("format").expects(STRING).validates(JsonSchemaType.STRING).build();
    /**
     * The value of this keyword MUST be a non-negative integer.
     * A string instance is valid against this keyword if its
     * length is less than, or equal to, the value of this keyword.
     * <p>
     * The length of a string instance is defined as the number of its
     * characters as defined by <a href="https://tools.ietf.org/html/RFC7159">RFC 7159</a>.
     */
    KeywordInfo<NumberKeyword> MAX_LENGTH = numberKeyword("maxLength").expects(NUMBER).validates(JsonSchemaType.STRING).build();
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
    KeywordInfo<NumberKeyword> MIN_LENGTH = numberKeyword("minLength").expects(NUMBER).validates(JsonSchemaType.STRING).build();
    /**
     * The value of this keyword MUST be a string. This string SHOULD be a
     * valid regular expression, according to the ECMA 262 regular expression
     * dialect.
     * <p>
     * A string instance is considered valid if the regular
     * expression matches the instance successfully. Recall: regular
     * expressions are not implicitly anchored.
     */
    KeywordInfo<StringKeyword> PATTERN = stringKeyword("pattern").expects(STRING).validates(JsonSchemaType.STRING).build();
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
    KeywordInfo<ItemsKeyword> ITEMS = keyword(ItemsKeyword.class).key("items").expects(ARRAY).validates(JsonSchemaType.ARRAY)
            .additionalDefinition().expects(OBJECT)
            .build();
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
    KeywordInfo<ItemsKeyword> ADDITIONAL_ITEMS = keyword(ItemsKeyword.class).key("additionalItems").expects(OBJECT).validates(JsonSchemaType.ARRAY).since(Draft3)
            .additionalDefinition().expects(TRUE).validates(JsonSchemaType.ARRAY).from(Draft3).until(Draft5)
            .additionalDefinition().expects(FALSE).validates(JsonSchemaType.ARRAY).from(Draft3).until(Draft5)
            .build();
    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An array instance is valid against "maxItems" if its size is
     * less than, or equal to, the value of this keyword.
     */
    KeywordInfo<NumberKeyword> MAX_ITEMS = numberKeyword("maxItems").expects(NUMBER).validates(JsonSchemaType.ARRAY).build();
    /**
     * The value of this keyword MUST be a non-negative integer.
     * <p>
     * An array instance is valid against "minItems" if its size is
     * greater than, or equal to, the value of this keyword.
     * <p>
     * Omitting this keyword has the same behavior as a value of 0.
     */
    KeywordInfo<NumberKeyword> MIN_ITEMS = numberKeyword("minItems").expects(NUMBER).validates(JsonSchemaType.ARRAY).build();
    /**
     * The value of this keyword MUST be a boolean.
     * <p>
     * If this keyword has boolean value false, the instance validates
     * successfully. If it has boolean value true, the instance validates
     * successfully if all of its elements are unique.
     * <p>
     * Omitting this keyword has the same behavior as a value of false.
     */
    KeywordInfo<BooleanKeyword> UNIQUE_ITEMS = booleanKeyword("uniqueItems").expects(TRUE).validates(JsonSchemaType.ARRAY)
            .additionalDefinition().expects(FALSE)
            .build();
    /**
     * The value of this keyword MUST be a valid JSON Schema.
     * <p>
     * An array instance is valid against "contains" if at least one of
     * its elements is valid against the given schema.
     */
    KeywordInfo<SingleSchemaKeyword> CONTAINS = singleSchemaKeyword("contains").expects(OBJECT).validates(JsonSchemaType.ARRAY).since(Draft6).build();
    /**
     * The value of this keyword MUST be an array. This array SHOULD have at
     * least one element. Elements in the array SHOULD be unique.
     * <p>
     * An instance validates successfully against this keyword if its value is
     * equal to one of the elements in this keyword's array value.
     * <p>
     * Elements in the array might be of any value, including null.
     */
    KeywordInfo<JsonArrayKeyword> ENUM = jsonArrayKeyword("enum").expects(ARRAY).build();
    /**
     * The value of this keyword MUST be an array. There are no restrictions placed on the values within the array.
     * <p>
     * This keyword can be used to provide sample JSON values associated with a particular schema, for the purpose
     * of illustrating usage. It is RECOMMENDED that these values be valid against the associated schema.
     * <p>
     * Implementations MAY use the value of "default", if present, as an additional example. If "examples" is
     * absent, "default" MAY still be used in this manner.
     */
    KeywordInfo<JsonArrayKeyword> EXAMPLES = jsonArrayKeyword("examples").expects(ARRAY).since(Draft6).build();
    /**
     * The value of this keyword MAY be of any type, including null.
     * <p>
     * An instance validates successfully against this keyword if its value is
     * equal to the value of the keyword.
     */
    KeywordInfo<JsonValueKeyword> CONST = jsonValueKeyword("const").expects(OBJECT).since(Draft6)
            .additionalDefinition().expects(ARRAY)
            .additionalDefinition().expects(STRING)
            .additionalDefinition().expects(NUMBER)
            .additionalDefinition().expects(TRUE)
            .additionalDefinition().expects(FALSE)
            .additionalDefinition().expects(NULL)
            .build();
    /**
     * This keyword's value MUST be a valid JSON Schema.
     * <p>
     * An instance is valid against this keyword if it fails to validate
     * successfully against the schema defined by this keyword.
     */
    KeywordInfo<SingleSchemaKeyword> NOT = singleSchemaKeyword("not").expects(OBJECT).since(Draft4).build();
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against all schemas defined by this keyword's value.
     */
    KeywordInfo<SchemaListKeyword> ALL_OF = schemaListKeyword("allOf").expects(ARRAY).since(Draft4).build();
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against at least one schema defined by this keyword's value.
     */
    KeywordInfo<SchemaListKeyword> ANY_OF = schemaListKeyword("anyOf").expects(ARRAY).since(Draft4).build();
    /**
     * This keyword's value MUST be a non-empty array.
     * Each item of the array MUST be a valid JSON Schema.
     * <p>
     * An instance validates successfully against this keyword if it validates
     * successfully against exactly one schema defined by this keyword's value.
     */
    KeywordInfo<SchemaListKeyword> ONE_OF = schemaListKeyword("oneOf").expects(ARRAY).since(Draft4).build();

    static KeywordInfo.KeywordInfoBuilder<SchemaMapKeyword> schemaMapKeyword(String keyword) {
        return new KeywordInfo.KeywordInfoBuilder<SchemaMapKeyword>().key(keyword);
    }

    static KeywordInfo.KeywordInfoBuilder<StringKeyword> stringKeyword(String keyword) {
        return new KeywordInfo.KeywordInfoBuilder<StringKeyword>().key(keyword);
    }

    static KeywordInfo.KeywordInfoBuilder<JsonValueKeyword> jsonValueKeyword(String keyword) {
        return new KeywordInfo.KeywordInfoBuilder<JsonValueKeyword>().key(keyword);
    }

    static KeywordInfoBuilder<NumberKeyword> numberKeyword(String keyword) {
        return new KeywordInfoBuilder<NumberKeyword>().key(keyword);
    }

    static KeywordInfoBuilder<StringSetKeyword> stringSetKeyword(String keyword) {
        return new KeywordInfoBuilder<StringSetKeyword>().key(keyword);
    }

    static KeywordInfoBuilder<SingleSchemaKeyword> singleSchemaKeyword(String keyword) {
        return new KeywordInfoBuilder<SingleSchemaKeyword>().key(keyword);
    }

    static KeywordInfoBuilder<SchemaListKeyword> schemaListKeyword(String keyword) {
        return new KeywordInfoBuilder<SchemaListKeyword>().key(keyword);
    }

    static KeywordInfoBuilder<URIKeyword> uriKeyword(String keyword) {
        return new KeywordInfoBuilder<URIKeyword>().key(keyword);
    }

    static KeywordInfoBuilder<BooleanKeyword> booleanKeyword(String keyword) {
        return new KeywordInfoBuilder<BooleanKeyword>().key(keyword);
    }

    static KeywordInfoBuilder<JsonArrayKeyword> jsonArrayKeyword(String keyword) {
        return new KeywordInfoBuilder<JsonArrayKeyword>().key(keyword);
    }

    static <X extends SchemaKeyword> KeywordInfoBuilder<X> keyword(Class<X> type) {
        return new KeywordInfoBuilder<X>();
    }

}
