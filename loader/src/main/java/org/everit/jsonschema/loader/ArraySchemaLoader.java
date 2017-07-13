package org.everit.jsonschema.loader;

import org.everit.json.JsonArray;
import org.everit.json.JsonElement;
import org.everit.json.UnexpectedValueException;
import org.everit.jsonschema.api.ArraySchema;
import org.everit.jsonschema.api.Schema;

import static java.util.Objects.requireNonNull;
import static org.everit.jsonschema.api.JsonSchemaProperty.ADDITIONAL_ITEMS;
import static org.everit.jsonschema.api.JsonSchemaProperty.ITEMS;
import static org.everit.jsonschema.api.JsonSchemaProperty.MAX_ITEMS;
import static org.everit.jsonschema.api.JsonSchemaProperty.MIN_ITEMS;
import static org.everit.jsonschema.api.JsonSchemaProperty.UNIQUE_ITEMS;
import static org.everit.jsonschema.api.JsonSchemaType.Array;
import static org.everit.jsonschema.api.JsonSchemaType.Boolean;
import static org.everit.jsonschema.api.JsonSchemaType.Object;

/**
 * @author erosb
 */
class ArraySchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public ArraySchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    ArraySchema.Builder load() {
        ArraySchema.Builder builder = ArraySchema.builder();
        ls.schemaJson.find(MIN_ITEMS).map(JsonElement::asInteger).ifPresent(builder::minItems);
        ls.schemaJson.find(MAX_ITEMS).map(JsonElement::asInteger).ifPresent(builder::maxItems);
        ls.schemaJson.find(UNIQUE_ITEMS).map(JsonElement::asBoolean).ifPresent(builder::uniqueItems);
        ls.schemaJson.find(ADDITIONAL_ITEMS).ifPresent(maybe -> {
            switch (maybe.schemaType()) {
                case Boolean:
                    builder.additionalItems(maybe.asBoolean());
                    break;
                case Object:
                    Schema childShema = defaultLoader.loadChild(maybe.asObject()).build();
                    builder.schemaOfAdditionalItems(childShema);
                    break;
                default:
                    throw new UnexpectedValueException(maybe, Boolean, Object);
            }
        });
        ls.schemaJson.find(ITEMS).ifPresent(items -> {
            switch (items.schemaType()) {
                case Object:
                    Schema childSchema = defaultLoader.loadChild(items.asObject()).build();
                    builder.allItemSchema(childSchema);
                    break;
                case Array:
                    buildTupleSchema(builder, items.asArray());
                    break;
                default:
                    throw new UnexpectedValueException(items, Object, Array);
            }
        });
        return builder;
    }

    private void buildTupleSchema(ArraySchema.Builder builder, JsonArray<?> itemSchema) {
        itemSchema.forEach(subschema -> {
            builder.addItemSchema(defaultLoader.loadChild(subschema.asObject()).build());
        });
    }
}
