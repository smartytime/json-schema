package io.dugnutt.json;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonPointer;
import javax.json.JsonString;
import javax.json.spi.JsonProvider;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseJsonProvider extends JsonProvider {
    /**
     * Creates a JSON object builder.
     *
     * @return a JSON object builder
     */
    @Override
    public JsonObjectBuilder createObjectBuilder() {
        return new MapBasedJsonObjectBuilder();
    }

    /**
     * Creates a JSON array builder.
     *
     * @return a JSON array builder
     */
    @Override
    public JsonArrayBuilder createArrayBuilder() {
        return new ListBasedArrayBuilder();
    }

    /**
     * Creates a builder factory for creating {@link JsonArrayBuilder}
     * and {@link JsonObjectBuilder} objects.
     * The factory is configured with the specified map of provider specific
     * configuration properties. Provider implementations should ignore any
     * unsupported configuration properties specified in the map.
     *
     * @param config a map of provider specific properties to configure the
     *               JSON extractors. The map may be empty or null
     * @return a JSON builder factory
     */
    @Override
    public JsonBuilderFactory createBuilderFactory(Map<String, ?> config) {
        return new JsonBuilderFactory() {
            @Override
            public JsonObjectBuilder createObjectBuilder() {
                return new MapBasedJsonObjectBuilder();
            }

            @Override
            public JsonArrayBuilder createArrayBuilder() {
                return new ListBasedArrayBuilder();
            }

            @Override
            public Map<String, ?> getConfigInUse() {
                return new HashMap<>();
            }
        };
    }

    @Override
    public JsonObjectBuilder createObjectBuilder(JsonObject object) {
        return new MapBasedJsonObjectBuilder(object);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder(JsonArray array) {
        return new ListBasedArrayBuilder(array);
    }

    @Override
    public JsonPointer createPointer(String jsonPointer) {
        return new BaseJsonPointer(jsonPointer);
    }

    @Override
    public JsonString createValue(String value) {
        return new JsonStringImpl(value);
    }

    @Override
    public JsonNumber createValue(int value) {
        return new JsonNumberImpl(value);
    }

    @Override
    public JsonNumber createValue(long value) {
        return new JsonNumberImpl(value);
    }

    @Override
    public JsonNumber createValue(double value) {
        return new JsonNumberImpl(value);
    }

    @Override
    public JsonNumber createValue(BigDecimal value) {
        return new JsonNumberImpl(value);
    }

    @Override
    public JsonNumber createValue(BigInteger value) {
        return new JsonNumberImpl(value);
    }
}
