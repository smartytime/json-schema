package io.sbsp.jsonschema.keyword;

import com.google.common.math.DoubleMath;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.EqualsAndHashCode;

import javax.json.JsonValue;
import java.net.URI;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode(of = "keywordValue")
public abstract class SchemaKeywordImpl<T> implements SchemaKeyword {
    private final T keywordValue;

    public SchemaKeywordImpl(T keywordValue) {
        checkNotNull(keywordValue, "keywordValue must not be null");
        this.keywordValue = keywordValue;
    }

    public T getKeywordValue() {
        return keywordValue;
    }

    protected T value() {
        return keywordValue;
    }

    @Override
    public void writeJson(KeywordInfo<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {

        final String jsonKey = keyword.key();
        if (keywordValue instanceof String) {
            generator.write(jsonKey, (String) keywordValue);
        } else if (keywordValue instanceof JsonValue) {
            generator.write(jsonKey, (JsonValue) keywordValue);
        } else if (keywordValue instanceof URI) {
            generator.write(jsonKey, keywordValue.toString());
        } else if (keywordValue instanceof Boolean) {
            generator.write(jsonKey, (Boolean) keywordValue);
        } else if (keywordValue instanceof Set) {
            generator.writeKey(jsonKey);
            generator.writeStartArray();
            ((Set<?>)keywordValue).forEach(item-> {
                generator.write(item.toString());
            });
            generator.writeEnd();
        } else if(keywordValue instanceof Number) {
            Number number = (Number) keywordValue;
            if (DoubleMath.isMathematicalInteger(number.doubleValue())) {
                generator.write(jsonKey, number.intValue());
            } else {
                generator.write(jsonKey, number.doubleValue());
            }
        } else {

            throw new RuntimeException("Unable to serialize JSON - unknown value type: " + keywordValue.getClass());
        }
    }

    @Override
    public String toString() {
        return String.valueOf(keywordValue);
    }
}
