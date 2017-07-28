package io.dugnutt.jsonschema.six;

import com.google.common.math.DoubleMath;

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

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword name, URI value) {
        if (value != null) {
            wrapped.write(name.key(), value.toString());
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword name, Integer value) {
        if (value != null) {
            wrapped.write(name.key(), value);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword name, String value) {
        if (value != null) {
            wrapped.write(name.key(), value);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword property, Schema schema) {
        if (schema != null) {
            write(property, schema);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword property, List<Schema> schemas) {
        if (schemas != null && schemas.size() > 0) {
            wrapped.writeKey(property.key());
            array();
            for (Schema schema : schemas) {
                schema.toJson(this);
            }
            endArray();
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword property, Map<String, Schema> schemas) {
        if (schemas != null && !schemas.isEmpty()) {
            writeKey(property);
            object();
            schemas.forEach((k, schema) -> {
                wrapped.writeKey(k);
                schema.toJson(this);
            });
            endObject();
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword property, Number number) {
        if (number != null) {
            if (DoubleMath.isMathematicalInteger(number.doubleValue())) {
                wrapped.write(property.key(), number.intValue());
            } else {
                wrapped.write(property.key(), number.doubleValue());
            }
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword property, Collection<String> arrayOfValues) {
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
            write(JsonSchemaKeyword.PATTERN, pattern.pattern());
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(JsonSchemaKeyword keyword, JsonValue value) {
        if (value != null) {
            wrapped.write(keyword.key(), value);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWrite(SchemaKeywords typeSpecificSchemaKeywords) {
        if (typeSpecificSchemaKeywords != null) {
            typeSpecificSchemaKeywords.toJson(this);
        }
        return this;
    }

    public JsonSchemaGenerator optionalWritePatternProperties(Map<Pattern, Schema> patterns) {
        if (patterns != null && !patterns.isEmpty()) {
            writeKey(JsonSchemaKeyword.PATTERN_PROPERTIES);
            object();
            patterns.forEach((pattern, schema) -> {
                wrapped.writeKey(String.valueOf(pattern));
                schema.toJson(this);
            });
            endObject();
        }
        return this;
    }

    public Consumer<JsonValue> jsonValueWriter(JsonSchemaKeyword keyword) {
        return value -> this.optionalWrite(keyword, value);
    }

    public JsonSchemaGenerator write(SchemaKeywords keywords) {
        keywords.toJson(this);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaKeyword name, String value) {
        wrapped.write(name.key(), value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaKeyword name, URI value) {
        checkNotNull(name, "name must not be null");
        checkNotNull(value, "value must not be null");

        wrapped.write(name.key(), value.toString());
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaKeyword name, Schema schema) {
        wrapped.writeKey(name.key());
        schema.toJson(this);
        return this;
    }

    public JsonSchemaGenerator write(String value) {
        wrapped.write(value);
        return this;
    }

    public JsonSchemaGenerator write(JsonSchemaKeyword keyword, JsonValue value) {
        wrapped.write(keyword.key(), value);
        return this;
    }

    public JsonSchemaGenerator writeIfTrue(JsonSchemaKeyword name, Boolean value) {
        if (value != null && value) {
            wrapped.write(name.key(), true);
        }
        return this;
    }

    public JsonSchemaGenerator writeKey(JsonSchemaKeyword name) {
        wrapped.writeKey(name.key());
        return this;
    }

    public JsonSchemaGenerator writePropertyName(String property) {
        wrapped.writeKey(property);
        return this;
    }

    public Consumer<? super Schema> schemaWriter(JsonSchemaKeyword keyword) {
        return schema -> write(keyword, schema);
    }

    public JsonSchemaGenerator writeSchemas(JsonSchemaKeyword property, List<Schema> schemas) {
        if (schemas != null && !schemas.isEmpty()) {
            wrapped.writeKey(property.key());
            array();
            for (Schema schema : schemas) {
                schema.toJson(this);
            }
            endArray();
        }
        return this;
    }

    public JsonSchemaGenerator writeType(JsonSchemaType type, boolean required) {
        if (required) {
            wrapped.write(JsonSchemaKeyword.TYPE.key(), type.toString());
        }
        return this;
    }
}
