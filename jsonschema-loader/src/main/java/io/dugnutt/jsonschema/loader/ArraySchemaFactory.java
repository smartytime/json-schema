package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.UnexpectedValueException;

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
    private final SchemaFactory schemaFactory;

    private ArraySchemaFactory(SchemaLoaderModel schemaModel, SchemaFactory schemaFactory) {
        this.schemaModel = requireNonNull(schemaModel, "schemaModel cannot be null");
        this.schemaFactory = requireNonNull(schemaFactory, "schemaFactory cannot be null");
    }

    public static ArraySchema.Builder createArraySchemaBuilder(SchemaLoaderModel schemaModel, SchemaFactory schemaFactory) {
        return new ArraySchemaFactory(schemaModel, schemaFactory).createArraySchemaBuilder();
    }

    ArraySchema.Builder createArraySchemaBuilder() {
        ArraySchema.Builder builder = ArraySchema.builder();
        final SchemaJsonObject schemaJson = schemaModel.schemaJson;

        schemaJson.findInteger(MIN_ITEMS).ifPresent(builder::minItems);
        schemaJson.findInt(MAX_ITEMS).ifPresent(builder::maxItems);
        schemaJson.findBoolean(UNIQUE_ITEMS).ifPresent(builder::uniqueItems);
        schemaJson.find(ADDITIONAL_ITEMS).ifPresent(maybe -> {
            switch (maybe.getValueType()) {
                case FALSE:
                    builder.additionalItems(false);
                    break;
                case TRUE:
                    builder.additionalItems(true);
                    break;
                case OBJECT:
                    final SchemaLoaderModel childSchemaModel = schemaModel.childModel(ADDITIONAL_ITEMS);
                    Schema childShema = schemaFactory.createSchema(childSchemaModel);
                    builder.schemaOfAdditionalItems(childShema);
                    break;
                default:
                    throw new UnexpectedValueException(maybe, TRUE, FALSE, OBJECT);
            }
        });

        schemaJson.find(ITEMS).ifPresent(items -> {
            switch (items.getValueType()) {
                case OBJECT:
                    final SchemaLoaderModel childSchemaModel = schemaModel.childModel(ITEMS);
                    final Schema childSchema = schemaFactory.createSchema(childSchemaModel);
                    builder.allItemSchema(childSchema);
                    break;
                case ARRAY:
                    schemaModel.streamArrayChildSchemas(ITEMS)
                            .map(schemaFactory::createSchema)
                            .forEach(builder::addItemSchema);
                    break;

                default:
                    throw new UnexpectedValueException(items, TRUE, FALSE, OBJECT);
            }
        });
        return builder;
    }
}
