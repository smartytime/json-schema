package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;

import javax.json.JsonValue;
import java.net.URI;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

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
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {

        final String jsonKey = keyword.getKey();
        if (keywordValue instanceof String) {
            generator.write(jsonKey, (String) keywordValue);
        } else if (keywordValue instanceof JsonValue) {
            generator.write(jsonKey, (JsonValue) keywordValue);
        } else if (keywordValue instanceof URI) {
            generator.write(jsonKey, keywordValue.toString());
        } else if (keywordValue instanceof Double) {
            generator.write(jsonKey, (Double) keywordValue);
        } else if (keywordValue instanceof Long) {
            generator.write(jsonKey, (Long) keywordValue);
        } else if (keywordValue instanceof Set) {
            generator.writeKey(jsonKey);
            generator.writeStartArray();
            ((Set<?>)keywordValue).forEach(item-> {
                generator.write(item.toString());
            });
            generator.writeEnd();
        } else {
            throw new RuntimeException("Unable to serialize JSON - unknown value type: " + keywordValue.getClass());
        }
    }
}
