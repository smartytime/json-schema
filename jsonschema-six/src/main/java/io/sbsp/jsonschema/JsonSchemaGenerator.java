package io.sbsp.jsonschema;

import com.google.common.math.DoubleMath;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;

import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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

    public JsonGenerator getWrapped() {
        return wrapped;
    }

    public JsonSchemaGenerator array() {
        wrapped.writeStartArray();
        return this;
    }

    public JsonSchemaGenerator endArray() {
        wrapped.writeEnd();
        return this;
    }

    public JsonSchemaGenerator endObject() {
        wrapped.writeEnd();
        return this;
    }

    public JsonSchemaGenerator object() {
        wrapped.writeStartObject();
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType name, Integer value) {
        if (value != null) {
            wrapped.write(name.key(), value);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType name, String value) {
        if (value != null) {
            wrapped.write(name.key(), value);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType name, URI value) {
        if (value != null) {
            wrapped.write(name.key(), value.toString());
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType property, Schema schema) {
        if (schema != null) {
            write(property, schema);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType property, List<Schema> schemas) {
        if (schemas != null && schemas.size() > 0) {
            wrapped.writeKey(property.key());
            array();
            for (Schema schema : schemas) {
                schema.toJson(wrapped);
            }
            endArray();
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType property, Map<String, Schema> schemas) {
        if (schemas != null && !schemas.isEmpty()) {
            writeKey(property);
            object();
            schemas.forEach((k, schema) -> {
                wrapped.writeKey(k);
                schema.toJson(wrapped);
            });
            endObject();
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType property, Number number) {
        if (number != null) {
            if (DoubleMath.isMathematicalInteger(number.doubleValue())) {
                wrapped.write(property.key(), number.intValue());
            } else {
                wrapped.write(property.key(), number.doubleValue());
            }
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType property, Collection<String> arrayOfValues) {
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

    public JsonSchemaGenerator optionalWrite(Pattern pattern) {
        if (pattern != null) {
            write(JsonSchemaKeywordType.PATTERN, pattern.pattern());
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeywordType keyword, JsonValue value) {
        if (value != null) {
            wrapped.write(keyword.key(), value);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWritePatternProperties(Map<Pattern, Schema> patterns) {
        if (patterns != null && !patterns.isEmpty()) {
            writeKey(JsonSchemaKeywordType.PATTERN_PROPERTIES);
            object();
            patterns.forEach((pattern, schema) -> {
                wrapped.writeKey(String.valueOf(pattern));
                schema.toJson(wrapped);
            });
            endObject();
        }
        return this;
    }

    public Consumer<JsonValue> jsonValueWriter(JsonSchemaKeywordType keyword) {
        return value -> this.optionalWrite(keyword, value);
    }

    public JsonSchemaGenerator write(JsonSchemaKeywordType name, String value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaKeywordType name, URI value) {
        checkNotNull(name, "name must not be null");
        checkNotNull(value, "value must not be null");

        wrapped.write(name.key(), value.toString());
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaKeywordType name, Schema schema) {
        wrapped.writeKey(name.key());
        schema.toJson(this.getWrapped());
        return this;
    }

    public JsonSchemaGenerator write(String value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaKeywordType keyword, JsonValue value) {
        wrapped.write(keyword.key(), value);
        return this;
    }

    public JsonSchemaGenerator writeIfTrue(JsonSchemaKeywordType name, Boolean value) {
        if (value != null && value) {
            wrapped.write(name.key(), true);
        }
        return this;
    }

    public JsonSchemaGenerator writeKey(JsonSchemaKeywordType name) {
        wrapped.writeKey(name.key());
        return this;
    }

    public JsonSchemaGenerator writePropertyName(String property) {
        wrapped.writeKey(property);
        return this;
    }

    public Consumer<? super Schema> schemaWriter(JsonSchemaKeywordType keyword) {
        return schema -> write(keyword, schema);
    }

    public JsonSchemaGenerator writeSchemas(JsonSchemaKeywordType property, List<Schema> schemas) {
        if (schemas != null && !schemas.isEmpty()) {
            wrapped.writeKey(property.key());
            array();
            for (Schema schema : schemas) {
                schema.toJson(wrapped);
            }
            endArray();
        }
        return this;
    }
}
