package org.martysoft.jsonschema.loader;

import org.martysoft.jsonschema.v6.ArraySchema;
import org.martysoft.jsonschema.v6.Schema;
import org.martysoft.jsonschema.v6.UnexpectedValueException;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;

import static java.util.Objects.requireNonNull;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.TRUE;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.ADDITIONAL_ITEMS;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.ITEMS;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.MAX_ITEMS;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.MIN_ITEMS;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.UNIQUE_ITEMS;

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
        ls.schemaJson.findNumber(MIN_ITEMS).map(JsonNumber::intValue).ifPresent(builder::minItems);
        ls.schemaJson.findNumber(MAX_ITEMS).map(JsonNumber::intValue).ifPresent(builder::maxItems);
        ls.schemaJson.findBoolean(UNIQUE_ITEMS).ifPresent(builder::uniqueItems);
        ls.schemaJson.find(ADDITIONAL_ITEMS).ifPresent(maybe -> {
            switch (maybe.getValueType()) {
                case FALSE:
                    builder.additionalItems(false);
                    break;
                case TRUE:
                    builder.additionalItems(true);
                    break;
                case OBJECT:
                    Schema childShema = defaultLoader.loadChild((JsonObject) maybe).build();
                    builder.schemaOfAdditionalItems(childShema);
                    break;
                default:
                    throw new UnexpectedValueException(maybe, TRUE, FALSE, OBJECT);
            }
        });

        ls.schemaJson.find(ITEMS).ifPresent(items -> {
            switch (items.getValueType()) {
                case OBJECT:
                    Schema childSchema = defaultLoader.loadChild((JsonObject) items).build();
                    builder.allItemSchema(childSchema);
                    break;
                case ARRAY:
                    buildTupleSchema(builder, (JsonArray) items);

                    break;

                default:
                    throw new UnexpectedValueException(items, TRUE, FALSE, OBJECT);
            }
        });
        return builder;
    }

    private void buildTupleSchema(ArraySchema.Builder builder, JsonArray itemSchema) {
        itemSchema.getValuesAs(JsonObject.class).forEach(subSchema -> {
            Schema loadedSubSchema = defaultLoader.loadChild(subSchema).build();
            builder.addItemSchema(loadedSubSchema);
        });
    }
}
