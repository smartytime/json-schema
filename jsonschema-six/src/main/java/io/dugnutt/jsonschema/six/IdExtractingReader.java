package io.dugnutt.jsonschema.six;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public class IdExtractingReader implements JsonParser {
    private final JsonParser wrapped;

    public IdExtractingReader(JsonParser wrapped) {
        this.wrapped = checkNotNull(wrapped);
    }

    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    @Override
    public Event next() {
        return wrapped.next();
    }

    @Override
    public String getString() {
        return wrapped.getString();
    }

    @Override
    public boolean isIntegralNumber() {
        return wrapped.isIntegralNumber();
    }

    @Override
    public int getInt() {
        return wrapped.getInt();
    }

    @Override
    public long getLong() {
        return wrapped.getLong();
    }

    @Override
    public BigDecimal getBigDecimal() {
        return wrapped.getBigDecimal();
    }

    @Override
    public JsonLocation getLocation() {
        return wrapped.getLocation();
    }

    @Override
    public JsonObject getObject() {
        return wrapped.getObject();
    }

    @Override
    public JsonValue getValue() {
        return wrapped.getValue();
    }

    @Override
    public JsonArray getArray() {
        return wrapped.getArray();
    }

    @Override
    public Stream<JsonValue> getArrayStream() {
        return wrapped.getArrayStream();
    }

    @Override
    public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
        return wrapped.getObjectStream();
    }

    @Override
    public Stream<JsonValue> getValueStream() {
        return wrapped.getValueStream();
    }

    @Override
    public void skipArray() {
        wrapped.skipArray();
    }

    @Override
    public void skipObject() {
        wrapped.skipObject();
    }

    @Override
    public void close() {
        wrapped.close();
    }
}
