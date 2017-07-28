package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.UnexpectedValueException;

import javax.json.JsonValue;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.UNIQUE_ITEMS;
import static javax.json.JsonValue.ValueType;

/**
 * @author erosb
 */
class ArrayKeywordsFactoryHelper {

    public static void appendArrayKeywords(PathAwareJsonValue schemaJson, Schema.JsonSchemaBuilder schemaBuilder,
                                           JsonSchemaFactory schemaFactory) {

        schemaJson.findInteger(MIN_ITEMS).ifPresent(schemaBuilder::minItems);
        schemaJson.findInt(MAX_ITEMS).ifPresent(schemaBuilder::maxItems);
        schemaJson.findBoolean(UNIQUE_ITEMS).ifPresent(schemaBuilder::needsUniqueItems);
        schemaJson.findPathAware(ADDITIONAL_ITEMS)
                .map(schemaFactory::createSchemaBuilder)
                .ifPresent(schemaBuilder::schemaOfAdditionalItems);

        schemaJson.findByKey(ITEMS).ifPresent(itemsValue -> {
            final JsonPath itemsPath = schemaJson.getPath().child(ITEMS.key());
            switch (itemsValue.getValueType()) {
                case OBJECT:
                    schemaBuilder.allItemSchema(
                            schemaFactory.createSchemaBuilder(itemsValue, itemsPath)
                    );
                    break;
                case ARRAY:
                    int idx = 0;
                    for (JsonValue jsonValue : itemsValue.asJsonArray()) {
                        final JsonPath idxPath = itemsPath.child(idx++);
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
}
