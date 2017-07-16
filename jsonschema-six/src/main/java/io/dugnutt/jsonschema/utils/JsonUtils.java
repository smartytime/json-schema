package io.dugnutt.jsonschema.utils;

import lombok.SneakyThrows;
import io.dugnutt.jsonschema.six.JsonSchemaType;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkNotNull;

public class JsonUtils {
    public static JsonObject readObject(String json) {
        checkNotNull(json, "json must not be null");
        return JsonProvider.provider()
                .createReader(new StringReader(json))
                .readObject();
    }

    @SneakyThrows
    public static JsonObject readObject(InputStream stream) {
        checkNotNull(stream, "stream must not be null");
        try (InputStream streamX = stream) {
            return JsonProvider.provider()
                    .createReader(streamX)
                    .readObject();
        }
    }

    @SneakyThrows
    public static JsonObject readObject(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return JsonProvider.provider().createReader(fileInputStream).readObject();
        }
    }

    @SneakyThrows
    public static <V extends JsonValue> V readValue(String json, Class<V> expected) {
        checkNotNull(json, "json must not be null");
        return (V) JsonProvider.provider()
                .createReader(new StringReader(json))
                .readValue();
    }

    @SneakyThrows
    public static <V extends JsonValue> V readValue(InputStream json, Class<V> expected) {
        checkNotNull(json, "json must not be null");
        return (V) JsonProvider.provider()
                .createReader(json)
                .readValue();
    }

    public static JsonObject blankObject() {
        return JsonProvider.provider().createObjectBuilder().build();
    }

    public static JsonArrayBuilder blankArrayBuilder() {
        return JsonProvider.provider().createArrayBuilder();
    }

    public static JsonObjectBuilder blankObjectBuilder() {
        return JsonProvider.provider().createObjectBuilder();
    }

    public static JsonSchemaType schemaTypeFor(JsonValue jsonValue) {
        checkNotNull(jsonValue, "jsonValue must not be null");
        JsonValue.ValueType valueType = jsonValue.getValueType();
        if (valueType == JsonValue.ValueType.FALSE || valueType == JsonValue.ValueType.TRUE) {
            return JsonSchemaType.BOOLEAN;
        } else {
            return JsonSchemaType.valueOf(UPPER_UNDERSCORE.to(UPPER_CAMEL, valueType.name()));
        }
    }

    public static <V extends JsonValue> V readResource(String resourceURL, Class<V> jsonValue) {
        return readValue(JsonUtils.class.getResourceAsStream(resourceURL), jsonValue);
    }
}
