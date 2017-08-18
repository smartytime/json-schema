package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaType;

import static io.sbsp.jsonschema.enums.JsonSchemaType.INTEGER;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.keyword.Keywords.singleSchemaKeyword;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;

public interface Draft3Keywords {

    //todo:ericm get docs
    KeywordInfo<NumberKeyword> DIVISIBLE_BY = Keywords.numberKeyword("divisibleBy").expects(NUMBER).validates(JsonSchemaType.NUMBER, INTEGER).onlyForVersion(Draft3).build();

    /**
     * From draft-3 schema
     * <p>
     * This attribute indicates if the instance must have a value, and not
     * be undefined.  This is false by default, making the instance
     * optional.
     */
    KeywordInfo<BooleanKeyword> REQUIRED_DRAFT3 = Keywords.booleanKeyword("required").expects(TRUE).onlyForVersion(Draft3)
            .additionalDefinition().expects(FALSE)
            .build();

    /**
     * From draft-03
     * <p>
     * This attribute takes the same values as the "type" attribute, however
     * if the instance matches the type or if this value is an array and the
     * instance matches any type or schema in the array, then this instance
     * is not valid.
     */
    KeywordInfo<TypeKeyword> DISALLOW = Keywords.keyword(TypeKeyword.class).expects(STRING).onlyForVersion(Draft3)
            .additionalDefinition().expects(ARRAY)
            .build();

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
    KeywordInfo<SingleSchemaKeyword> EXTENDS = singleSchemaKeyword("extends").expects(OBJECT).onlyForVersion(Draft3).build();

}
