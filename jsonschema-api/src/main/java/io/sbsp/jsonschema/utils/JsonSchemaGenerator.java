package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.keyword.KeywordMetadata;
import lombok.experimental.Delegate;

import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import java.math.BigDecimal;

public class JsonSchemaGenerator implements JsonGenerator {
    @Delegate
    private final JsonGenerator wrapped;

    public JsonSchemaGenerator(JsonGenerator wrapped) {
        this.wrapped = wrapped;
    }

    public JsonSchemaGenerator write(String key, Number number) {
        if (number == null) {
            write(key, JsonValue.NULL);
        } else {
            final BigDecimal intermediate = new BigDecimal(number.doubleValue());
            if (intermediate.scale() == 0) {
                write(key, intermediate.intValueExact());
            } else {
                write(key, intermediate.doubleValue());
            }
        }
        return this;
    }

    public JsonSchemaGenerator writeKey(KeywordMetadata<?> keyword) {
        wrapped.writeKey(keyword.getKey());
        return this;
    }
}
