package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.Schema;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.UNIQUE_ITEMS;
import static java.util.Objects.requireNonNull;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.TRUE;

/**
 * @author erosb
 */
class ArraySchemaFactory {

    private final SchemaLoaderModel schemaModel;
    private final JsonSchemaFactory schemaFactory;

    private ArraySchemaFactory(SchemaLoaderModel schemaModel, JsonSchemaFactory schemaFactory) {
        this.schemaModel = requireNonNull(schemaModel, "schemaModel cannot be null");
        this.schemaFactory = requireNonNull(schemaFactory, "schemaFactory cannot be null");
    }

    public static ArraySchema.Builder createArraySchemaBuilder(SchemaLoaderModel schemaModel, JsonSchemaFactory schemaFactory) {
        return new ArraySchemaFactory(schemaModel, schemaFactory).createArraySchemaBuilder();
    }

    ArraySchema.Builder createArraySchemaBuilder() {
        ArraySchema.Builder builder = ArraySchema.builder();
        final FluentJsonObject schemaJson = schemaModel.schemaJson;

        schemaJson.findInteger(MIN_ITEMS).ifPresent(builder::minItems);
        schemaJson.findInt(MAX_ITEMS).ifPresent(builder::maxItems);
        schemaJson.findBoolean(UNIQUE_ITEMS).ifPresent(builder::uniqueItems);
        schemaModel.childModel(ADDITIONAL_ITEMS)
                .map(schemaFactory::createSchema)
                .ifPresent(builder::schemaOfAdditionalItems);


        schemaJson.find(ITEMS).ifPresent(items -> {
            switch (items.getValueType()) {
                case OBJECT:
                    final SchemaLoaderModel childSchemaModel = schemaModel.childModel(ITEMS);
                    final Schema childSchema = schemaFactory.createSchema(childSchemaModel);
                    builder.allItemSchema(childSchema);
                    break;
                case ARRAY:
                    schemaModel.streamChildSchemaModelsForArray(ITEMS, items.asJsonArray())
                            .map(schemaFactory::createSchema)
                            .forEach(builder::addItemSchema);
                    break;

                default:
                    throw schemaModel.unexpectedValueException(ITEMS, items, TRUE, FALSE, OBJECT);
            }
        });
        return builder;
    }
}
