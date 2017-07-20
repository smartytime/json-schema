package io.dugnutt.jsonschema.utils;

import io.dugnutt.jsonschema.six.JsonSchemaType;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;
import static javax.json.spi.JsonProvider.provider;

public class JsonUtils {
    public static JsonArray blankJsonArray() {
        return provider().createArrayBuilder().build();
    }

    public static JsonObject blankJsonObject() {
        return provider().createObjectBuilder().build();
    }

    @Nullable
    public static URI extract$IdFromObject(JsonObject json) {
        checkNotNull(json, "json must not be null");
        JsonValue $idValue = json.get($ID.key());
        if ($idValue != null && $idValue.getValueType() == JsonValue.ValueType.STRING) {
            return URI.create(((JsonString) $idValue).getString());
        } else {
            return null;
        }
    }

    public static Object extract(JsonValue v) {
        checkNotNull(v, "v must not be null");
        switch (v.getValueType()) {
            case FALSE:
                return false;
            case TRUE:
                return true;
            case NULL:
                return null;
            case STRING:
                return ((JsonString) v).getString();
            case NUMBER:
                return ((JsonNumber) v).numberValue();
            case ARRAY:
                return extractArray(v.asJsonArray());
            case OBJECT:
                return extractObject(v.asJsonObject());
            default:
                throw new IllegalArgumentException("Can only extract from simple types");
        }
    }

    public static List<Object> extractArray(JsonArray jsonArray) {
        checkNotNull(jsonArray, "jsonArray must not be null");

        return jsonArray.stream()
                .map(JsonUtils::extract)
                .collect(Collectors.toList());
    }

    public static Map<String, Object> extractObject(JsonObject jsonObject) {
        checkNotNull(jsonObject, "jsonObject must not be null");
        final Map<String, Object> rtn = new LinkedHashMap<>();
        jsonObject.forEach((k, v) -> rtn.put(k, extract(v)));
        return rtn;
    }

    public static JsonArray jsonArray(List<Object> values) {
        return provider().createArrayBuilder(values).build();
    }

    public static JsonArray jsonArray(Object... values) {
        return provider().createArrayBuilder(Arrays.asList(values)).build();
    }

    public static JsonArrayBuilder jsonArrayBuilder() {
        return provider().createArrayBuilder();
    }

    public static JsonObject jsonObject(Map<String, Object> values) {
        return provider().createObjectBuilder(values).build();
    }

    public static JsonObjectBuilder jsonObjectBuilder() {
        return provider().createObjectBuilder();
    }

    public static JsonString jsonStringValue(String value) {
        return provider().createValue(value);
    }

    public static JsonNumber jsonNumberValue(double num) {
        return provider().createValue(num);
    }

    public static JsonNumber jsonNumberValue(long num) {
        return provider().createValue(num);
    }

    public static JsonObject readJsonObject(String json) {
        checkNotNull(json, "json must not be null");
        return provider()
                .createReader(new StringReader(json))
                .readObject();
    }

    @SneakyThrows
    public static JsonObject readJsonObject(InputStream stream) {
        checkNotNull(stream, "stream must not be null");
        try (InputStream streamX = stream) {
            return provider()
                    .createReader(streamX)
                    .readObject();
        }
    }

    @SneakyThrows
    public static JsonObject readJsonObject(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return provider().createReader(fileInputStream).readObject();
        }
    }

    public static <V extends JsonValue> V readResourceAsJson(String resourceURL, Class<V> jsonValue) {
        return readValue(JsonUtils.class.getResourceAsStream(resourceURL), jsonValue);
    }

    public static boolean isOneOf(JsonValue value, JsonValue.ValueType... anyOf) {
        if (anyOf.length == 0) {
            return true;
        }
        for (JsonValue.ValueType valueType : anyOf) {
            if (value.getValueType() == valueType) {
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public static <V extends JsonValue> V readValue(String json, Class<V> expected) {
        checkNotNull(json, "json must not be null");
        return (V) provider()
                .createReader(new StringReader(json))
                .readValue();
    }

    @SneakyThrows
    public static JsonValue readValue(String json) {
        checkNotNull(json, "json must not be null");
        return provider()
                .createReader(new StringReader(json))
                .readValue();
    }

    @SneakyThrows
    public static <V extends JsonValue> V readValue(InputStream json, Class<V> expected) {
        checkNotNull(json, "json must not be null");
        return (V) provider()
                .createReader(json)
                .readValue();
    }

    public static JsonValue.ValueType jsonTypeForClass(Class<? extends JsonValue> clazz) {
        if (clazz.isAssignableFrom(JsonNumber.class)) {
            return JsonValue.ValueType.NUMBER;
        } else if (clazz.isAssignableFrom(JsonString.class)) {
            return JsonValue.ValueType.STRING;
        } else if (clazz.isAssignableFrom(JsonObject.class)) {
            return JsonValue.ValueType.OBJECT;
        } else if (clazz.isAssignableFrom(JsonArray.class)) {
            return JsonValue.ValueType.ARRAY;
        } else {
            throw new IllegalArgumentException("Unable to determine type for class: " + clazz);
        }
    }

    public static JsonSchemaType schemaTypeFor(JsonValue jsonValue) {
        checkNotNull(jsonValue, "jsonValue must not be null");
        JsonValue.ValueType valueType = jsonValue.getValueType();
        if (valueType == JsonValue.ValueType.FALSE || valueType == JsonValue.ValueType.TRUE) {
            return JsonSchemaType.BOOLEAN;
        } else {
            return JsonSchemaType.valueOf(valueType.name());
        }
    }
}
