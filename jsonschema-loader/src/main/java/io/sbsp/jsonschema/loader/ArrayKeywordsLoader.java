package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.UnexpectedValueException;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.UNIQUE_ITEMS;
import static javax.json.JsonValue.ValueType;

/**
 * @author erosb
 */
class ArrayKeywordsLoader implements KeywordsLoader {
    private ArrayKeywordsLoader() {

    }

    @Override
    public void appendKeywords(JsonValueWithLocation schemaJson, Schema.JsonSchemaBuilder schemaBuilder, JsonSchemaFactory schemaFactory) {

        schemaJson.findInteger(MIN_ITEMS).ifPresent(schemaBuilder::minItems);
        schemaJson.findInt(MAX_ITEMS).ifPresent(schemaBuilder::maxItems);
        schemaJson.findBoolean(UNIQUE_ITEMS).ifPresent(schemaBuilder::needsUniqueItems);
        schemaJson.findPathAwareObject(ADDITIONAL_ITEMS)
                .map(schemaFactory::createSchemaBuilder)
                .ifPresent(schemaBuilder::schemaOfAdditionalItems);

        schemaJson.findByKey(ITEMS).ifPresent(itemsValue -> {
            final SchemaLocation itemsPath = schemaJson.getLocation().child(ITEMS);
            switch (itemsValue.getValueType()) {
                case OBJECT:
                    schemaBuilder.allItemSchema(
                            schemaFactory.createSchemaBuilder(itemsValue, itemsPath)
                    );
                    break;
                case ARRAY:
                    int idx = 0;
                    for (JsonValue jsonValue : itemsValue.asJsonArray()) {
                        final SchemaLocation idxPath = itemsPath.child(idx++);
                        if (jsonValue.getValueType() != ValueType.OBJECT) {
                            throw new UnexpectedValueException(idxPath, itemsValue, ValueType.OBJECT);
                        }
                        schemaBuilder.itemSchema(
                                schemaFactory.createSchemaBuilder(jsonValue, idxPath)
                        );
                    }
                    break;
                default:
                    throw new UnexpectedValueException(itemsPath, itemsValue, ValueType.OBJECT, ValueType.ARRAY);
            }
        });
    }

    public static ArrayKeywordsLoader arrayKeywordsLoader() {
        return new ArrayKeywordsLoader();
    }
}
