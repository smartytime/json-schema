package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.UnexpectedValueException;

import javax.json.JsonString;
import javax.json.JsonValue.ValueType;
import java.util.Arrays;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ALL_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ANY_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONST;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DEFAULT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DESCRIPTION;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ENUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.NOT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ONE_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TITLE;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TYPE;

public class SharedKeywordsLoader implements KeywordsLoader {
    private SharedKeywordsLoader() {

    }

    @Override
    public void appendKeywords(JsonValueWithLocation schemaJson, Schema.JsonSchemaBuilder schemaBuilder, JsonSchemaFactory factory) {

        // ############################
        // type: either string or array
        // ############################

        schemaJson.findByKey(TYPE).ifPresent(typeJsonValue -> {
            final JsonPath typePath = schemaJson.getPath().child(TYPE.key());
            switch (typeJsonValue.getValueType()) {
                case STRING:
                    final JsonSchemaType typeEnum;
                    try {
                        typeEnum = JsonSchemaType.fromString(schemaJson.getString(TYPE));
                    } catch (SchemaException e) {
                        throw new SchemaException(typePath.toURIFragment(), e.getMessage());
                    }
                    schemaBuilder.type(typeEnum);
                    break;
                case ARRAY:
                    schemaJson.expectArray(TYPE).getValuesAs(JsonString.class).stream()
                            .map(JsonString::getString)
                            .map(JsonSchemaType::fromString)
                            .forEach(schemaBuilder::type);
                    break;
                default:
                    throw new UnexpectedValueException(typePath, typeJsonValue, ValueType.STRING, ValueType.ARRAY);
            }
        });

        // ############################
        // title, description
        // ############################

        schemaJson.findByKey(DEFAULT).ifPresent(schemaBuilder::defaultValue);
        schemaJson.findString(TITLE).ifPresent(schemaBuilder::title);
        schemaJson.findString(DESCRIPTION).ifPresent(schemaBuilder::description);

        // ##########################
        // const, enum, not
        // ##########################

        schemaJson.findByKey(CONST).ifPresent(schemaBuilder::constValue);
        schemaJson.findArray(ENUM).ifPresent(schemaBuilder::enumValues);
        schemaJson.findPathAwareObject(NOT).map(factory::createSchemaBuilder).ifPresent(schemaBuilder::notSchema);

        // #########################
        // allOf, anyOf, oneOf
        // #########################

        Arrays.asList(ONE_OF, ANY_OF, ALL_OF).forEach(keyword -> schemaJson.streamPathAwareArrayItems(keyword)
                .map(factory::createSchemaBuilder)
                .forEach(combinedSchema -> schemaBuilder.combinedSchema(keyword, combinedSchema)));
    }

    public static SharedKeywordsLoader sharedKeywordsLoader() {
        return new SharedKeywordsLoader();
    }
}
