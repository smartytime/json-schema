package org.martysoft.jsonschema.v6;

import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convenience class for making it easy to write optional properties to an existing json generator.  Wraps an
 * underlying {@link JsonGenerator}
 */
public class JsonSchemaGenerator {

    private JsonGenerator wrapped;

    public JsonSchemaGenerator(JsonGenerator wrapped) {
        this.wrapped = checkNotNull(wrapped);
    }

    public JsonSchemaGenerator array() {
        wrapped.writeStartArray();
        return this;
    }

    public void close() {
        wrapped.close();
    }

    public JsonSchemaGenerator endArray() {
        wrapped.writeEnd();
        return this;
    }

    public JsonSchemaGenerator endObject() {
        wrapped.writeEnd();
        return this;
    }

    public void flush() {
        wrapped.flush();
    }

    public JsonGenerator getWrapped() {
        return wrapped;
    }

    public JsonSchemaGenerator object() {
        wrapped.writeStartObject();
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaProperty name, Integer value) {
        if (value != null) {
            wrapped.write(name.key(), value);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaProperty name, String value) {
        if (value != null) {
            wrapped.write(name.key(), value);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaProperty property, Schema schema) {
        if (schema != null) {
            write(property, schema);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaProperty property, Collection<Schema> schemas) {
        if (schemas != null) {
            wrapped.writeKey(property.key());
            array();
            for (Schema schema : schemas) {
                schema.propertiesToJson(this);
            }
            endArray();
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaProperty property, Map<String, Schema> schemas) {
        if (schemas != null && !schemas.isEmpty()) {
            writeKey(property);
            object();
            schemas.forEach((k, schema) -> {
                wrapped.writeKey(k);
                schema.propertiesToJson(this);
            });
            endObject();
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaProperty property, Number number) {
        if (number != null) {
            wrapped.write(property.key(), number.doubleValue());
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaProperty property, List<String> arrayOfValues) {
        if (arrayOfValues != null && !arrayOfValues.isEmpty()) {
            wrapped.writeKey(property.key());
            array();
            for (String value : arrayOfValues) {
                wrapped.write(value);
            }
            endArray();
        }
        return this;
    }

    public void optionalWrite(FormatType format) {
        if (format != null) {
            write(JsonSchemaProperty.FORMAT, format.toString());
        }
    }

    public JsonSchemaGenerator optionalWrite(Pattern pattern) {
        if (pattern != null) {
            write(JsonSchemaProperty.PATTERN, pattern.pattern());
        }
        return this;
    }

    public JsonSchemaGenerator optionalWritePatternProperties(Map<Pattern, Schema> patterns) {
        if (patterns != null && !patterns.isEmpty()) {
            writeKey(JsonSchemaProperty.PATTERN_PROPERTIES);
            object();
            patterns.forEach((pattern, schema) -> {
                wrapped.writeKey(String.valueOf(pattern));
                schema.propertiesToJson(this);
            });
            endObject();
        }
        return this;
    }

    public JsonSchemaGenerator pattern(Pattern pattern) {
        wrapped.writeKey(pattern.pattern());
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty name, JsonValue value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty name, String value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty name, BigInteger value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty name, BigDecimal value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty name, int value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty name, long value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty name, double value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty name, boolean value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonValue value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(String value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(BigDecimal value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(BigInteger value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(int value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(long value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(double value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(boolean value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaProperty property, Schema schema) {
        wrapped.writeKey(property.key());
        schema.toJson(this);
        return this;
    }

    public JsonSchemaGenerator writeEnd() {
        wrapped.writeEnd();
        return this;
    }

    public JsonSchemaGenerator writeIfFalse(JsonSchemaProperty name, Boolean value) {
        if (value == null || !value) {
            wrapped.write(name.key(), false);
        }
        return this;
    }

    public JsonSchemaGenerator writeIfTrue(JsonSchemaProperty name, Boolean value) {
        if (value != null && value) {
            wrapped.write(name.key(), true);
        }
        return this;
    }

    public JsonSchemaGenerator writeKey(JsonSchemaProperty name) {
        wrapped.writeKey(name.key());
        return this;
    }

    public JsonSchemaGenerator writeNull(JsonSchemaProperty name) {
        wrapped.writeNull(name.key());
        return this;
    }

    public JsonSchemaGenerator writeNull() {
        wrapped.writeNull();
        return this;
    }

    public JsonSchemaGenerator writePropertyName(String property) {
        wrapped.writeKey(property);
        return this;
    }

    public JsonSchemaGenerator writeStartArray() {
        wrapped.writeStartArray();
        return this;
    }

    public JsonSchemaGenerator writeStartArray(JsonSchemaProperty name) {
        wrapped.writeStartArray(name.key());
        return this;
    }

    public JsonSchemaGenerator writeStartObject(JsonSchemaProperty name) {
        wrapped.writeStartObject(name.key());
        return this;
    }

    public JsonSchemaGenerator writeType(JsonSchemaType type, boolean required) {
        if (required) {
            wrapped.write(JsonSchemaProperty.TYPE.key(), type.toString());
        }
        return this;
    }
}
