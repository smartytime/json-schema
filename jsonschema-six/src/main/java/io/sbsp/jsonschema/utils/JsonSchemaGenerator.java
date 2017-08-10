package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.BooleanKeyword;
import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.JsonValueKeyword;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.MaximumKeyword;
import io.sbsp.jsonschema.keyword.MinimumKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.StringKeyword;
import lombok.experimental.Delegate;

import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import java.math.BigDecimal;
import java.util.Set;

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

    public JsonSchemaGenerator writeType(JsonSchemaType type) {
        wrapped.write(SchemaKeyword.type.getKey(), type.toString());
        return this;
    }

    public JsonSchemaGenerator writeTypes(Set<JsonSchemaType> types) {
        wrapped.writeKey(SchemaKeyword.type.getKey());
        wrapped.writeStartArray();
        for (JsonSchemaType type : types) {
            write(type.toString());
        }
        wrapped.writeEnd();
        return this;
    }

    public JsonSchemaGenerator writeMax(KeywordMetadata<MaximumKeyword> keyword, Number value) {
        return write(keyword.getKey(), value);
    }

    public JsonSchemaGenerator writeMin(KeywordMetadata<MinimumKeyword> keyword, Number value) {
        return write(keyword.getKey(), value);
    }

    public JsonGenerator write(KeywordMetadata<BooleanKeyword> keyword, Boolean value) {
        return write(keyword.getKey(), value);
    }

    public JsonGenerator writeMax(KeywordMetadata<MaximumKeyword> keyword, Boolean value) {
        return write(keyword.getKey(), value);
    }

    public JsonGenerator writeMin(KeywordMetadata<MinimumKeyword> keyword, Boolean value) {
        return write(keyword.getKey(), value);
    }

    public JsonGenerator write(KeywordMetadata<StringKeyword> keyword, String value) {
        return write(keyword.getKey(), value);
    }

    public JsonGenerator write(KeywordMetadata<JsonArrayKeyword> keyword, JsonArray value) {
        return write(keyword.getKey(), value);
    }

    public JsonGenerator write(KeywordMetadata<JsonValueKeyword> keyword, JsonValue value) {
        return write(keyword.getKey(), value);
    }

    public JsonSchemaGenerator writeKey(KeywordMetadata<?> keyword) {
        wrapped.writeKey(keyword.getKey());
        return this;
    }
}
