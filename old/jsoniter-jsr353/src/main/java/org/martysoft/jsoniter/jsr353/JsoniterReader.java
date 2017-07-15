package org.martysoft.jsoniter.jsr353;

import com.google.common.io.CharStreams;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

public class JsoniterReader implements JsonReader {

    @Nullable
    private final Any any;

    @Nullable
    private final JsonException jsonException;

    JsoniterReader(String inputJson) {
        checkNotNull(inputJson, "inputJson must not be null");
        this.any = JsonIterator.deserialize(inputJson);
        jsonException = null;
    }

    JsoniterReader(InputStream inputStream, Charset charset) {
        this(new InputStreamReader(inputStream, charset));
    }

    JsoniterReader(Reader reader) {
        JsonException jsonException = null;
        Any any = null;

        String jsonString;

        try {
            jsonString = CharStreams.toString(reader);
            try {
                any = JsonIterator.deserialize(jsonString);
            } catch (com.jsoniter.spi.JsonException e) {
                jsonException = new JsonException(e.getMessage(), e);
            }
        } catch (IOException e) {
            jsonException = new JsonException(e.getMessage(), e);
        }

        this.jsonException = jsonException;
        this.any = any;
    }

    JsoniterReader(InputStream inputStream) {
        this(inputStream, UTF_8);
    }

    @Override
    public JsonStructure read() {
        checkState();
        return (JsonStructure) JsoniterHelper.wrapAny(this.any);
    }

    @Override
    public JsonObject readObject() {
        checkState();
        return (JsonObject) JsoniterHelper.wrapAny(this.any);
    }

    @Override
    public JsonArray readArray() {
        checkState();
        return (JsonArray) JsoniterHelper.wrapAny(this.any);
    }

    @Override
    public void close() {
        //Nothing to do.
    }

    private void checkState() {
        if (jsonException != null) {
            throw jsonException;
        }
    }
}
