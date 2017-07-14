package org.everit.jsonschema.utils;

import lombok.SneakyThrows;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;

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
        try(InputStream streamX = stream) {
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
}
