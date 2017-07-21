package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.UNIQUE_ITEMS;
import static java.util.Objects.requireNonNull;
import static javax.json.JsonValue.ValueType.ARRAY;

/**
 * @author erosb
 */
class ArraySchemaFactory {

    private final SchemaLoadingContext schemaModel;
    private final JsonSchemaFactory schemaFactory;

    private ArraySchemaFactory(SchemaLoadingContext schemaModel, JsonSchemaFactory schemaFactory) {
        this.schemaModel = requireNonNull(schemaModel, "schemaModel cannot be null");
        this.schemaFactory = requireNonNull(schemaFactory, "schemaFactory cannot be null");
    }

    public static ArraySchema.Builder createArraySchemaBuilder(SchemaLoadingContext schemaModel, JsonSchemaFactory schemaFactory) {
        return new ArraySchemaFactory(schemaModel, schemaFactory).createArraySchemaBuilder();
    }

    ArraySchema.Builder createArraySchemaBuilder() {
        ArraySchema.Builder builder = ArraySchema.builder();
        builder.location(schemaModel.getLocation());

        final PathAwareJsonValue schemaJson = schemaModel.schemaJson;
        schemaJson.findInteger(MIN_ITEMS).ifPresent(builder::minItems);
        schemaJson.findInt(MAX_ITEMS).ifPresent(builder::maxItems);
        schemaJson.findBoolean(UNIQUE_ITEMS).ifPresent(builder::uniqueItems);
        schemaModel.childModel(ADDITIONAL_ITEMS)
                .map(schemaFactory::createSchema)
                .ifPresent(builder::schemaOfAdditionalItems);

        schemaModel.childModelIfObject(ITEMS)
                .map(schemaFactory::createSchema)
                .ifPresent(builder::allItemSchema);

        if(schemaModel.isPropertyType(ITEMS, ARRAY)) {
            schemaModel.streamChildSchemaModels(ITEMS)
                    .map(schemaFactory::createSchema)
                    .forEach(builder::addItemSchema);
        }

        return builder;
    }
}
