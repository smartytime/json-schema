package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonSchemaProperty;
import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.UnexpectedValueException;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;

import static java.util.Objects.requireNonNull;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.TRUE;

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
        ls.schemaJson.findNumber(JsonSchemaProperty.MIN_ITEMS).map(JsonNumber::intValue).ifPresent(builder::minItems);
        ls.schemaJson.findNumber(JsonSchemaProperty.MAX_ITEMS).map(JsonNumber::intValue).ifPresent(builder::maxItems);
        ls.schemaJson.findBoolean(JsonSchemaProperty.UNIQUE_ITEMS).ifPresent(builder::uniqueItems);
        ls.schemaJson.find(JsonSchemaProperty.ADDITIONAL_ITEMS).ifPresent(maybe -> {
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

        ls.schemaJson.find(JsonSchemaProperty.ITEMS).ifPresent(items -> {
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
