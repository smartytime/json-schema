package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaType;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.EXTENDS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.REQUIRED_PROPERTY;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft5;
import static io.sbsp.jsonschema.keyword.Keywords.keyword;
import static io.sbsp.jsonschema.keyword.Keywords.singleSchemaKeyword;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.TRUE;

public interface Draft4SchemaKeyword  {



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
     KeywordMetadata<BooleanKeyword> requiredDraft3 = Keywords.booleanKeyword(REQUIRED_PROPERTY).expects(TRUE, FALSE).validatesAnyType().from(Draft3).until(Draft3).build();

    /**
     * From draft-03
     * <p>
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "maximum" attribute.  This is false by default, meaning the instance
     * value can be less then or equal to the maximum value.
     */
     KeywordMetadata<LimitKeyword> exclusiveMaximumDraft4 = keyword(LimitKeyword.class).key("exclusiveMaximum").expects(TRUE, FALSE).validates(JsonSchemaType.NUMBER).from(Draft3).until(Draft5).build();

    /**
     * From draft-03
     * <p>
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "minimum" attribute.  This is false by default, meaning the instance
     * value can be greater then or equal to the minimum value.
     */
    KeywordMetadata<LimitKeyword> exclusiveMinimumDraft4 = keyword(LimitKeyword.class).key("exclusiveMinimum").expects(TRUE, FALSE).validates(JsonSchemaType.NUMBER).from(Draft3).until(Draft5).build();

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
