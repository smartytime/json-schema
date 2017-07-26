package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue.ValueType;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.UNIQUE_ITEMS;

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
        schemaJson.findPathAware(ITEMS)
                .filter(jsonValue->jsonValue.is(ValueType.OBJECT))
                .map(schemaFactory::createSchemaBuilder)
                .ifPresent(schemaBuilder::allItemSchema);
        schemaJson.streamPathAwareArrayItems(ITEMS)
                .map(schemaFactory::createSchemaBuilder)
                .forEach(schemaBuilder::itemSchema);
    }
}
