package io.sbsp.jsonschema.utils;

import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGeneratorFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$ID;
import static javax.json.JsonValue.EMPTY_JSON_ARRAY;
import static javax.json.spi.JsonProvider.provider;
import static javax.json.stream.JsonGenerator.PRETTY_PRINTING;

public class JsonUtils {

    private static final Map<String, ?> PRETTY_PRINT_OPTS = ImmutableMap.of(PRETTY_PRINTING, true);
    private static final JsonWriterFactory PRETTY_PRINT_WRITER_FACTORY = JsonProvider.provider().createWriterFactory(PRETTY_PRINT_OPTS);
    private static final JsonGeneratorFactory PRETTY_PRINT_GENERATOR_FACTORY = JsonProvider.provider().createGeneratorFactory(PRETTY_PRINT_OPTS);

    public static JsonArray emptyJsonArray() {
        return EMPTY_JSON_ARRAY;
    }

    public static JsonArray blankJsonArray() {
        return provider().createArrayBuilder().build();
    }

    public static JsonObject blankJsonObject() {
        return provider().createObjectBuilder().build();
    }

    @Nullable
    public static URI extract$IdFromObject(JsonObject json) {
        checkNotNull(json, "json must not be null");
        if (json.containsKey($ID.key())) {
            JsonValue $idValue = json.get($ID.key());
            if ($idValue != null && $idValue.getValueType() == JsonValue.ValueType.STRING) {
                return URI.create(((JsonString) $idValue).getString());
            }
        }
        return null;
    }

    public static JsonArray jsonArray(List<Object> values) {
        return provider().createArrayBuilder(values).build();
    }

    public static JsonArray jsonArray(Object... values) {
        return provider().createArrayBuilder(Arrays.asList(values)).build();
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

    public static JsonGeneratorFactory prettyPrintGeneratorFactory() {
        return PRETTY_PRINT_GENERATOR_FACTORY;
    }

    public static String toPrettyString(JsonValue value, boolean indent) {
        checkNotNull(value, "value must not be null");
        final StringWriter strings = new StringWriter();
        final Writer actualWriter;
        if (indent) {
            actualWriter = new IndentingWriter(strings, "\t");
        } else {
            actualWriter = strings;
        }

        PRETTY_PRINT_WRITER_FACTORY.createWriter(actualWriter).write(value);
        strings.flush();
        return strings.toString();
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

    @SneakyThrows
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
        switch (valueType) {
            case ARRAY:
                return JsonSchemaType.ARRAY;
            case OBJECT:
                return JsonSchemaType.OBJECT;
            case STRING:
                return JsonSchemaType.STRING;
            case NUMBER:
                return JsonSchemaType.NUMBER;
            case FALSE:
                return JsonSchemaType.BOOLEAN;
            case TRUE:
                return JsonSchemaType.BOOLEAN;
            case NULL:
                return JsonSchemaType.NULL;
            default:
                throw new IllegalArgumentException("Unable to determine type");
        }
    }
}
