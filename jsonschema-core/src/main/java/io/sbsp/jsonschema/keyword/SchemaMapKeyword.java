package io.sbsp.jsonschema.keyword;

import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@EqualsAndHashCode
public class SchemaMapKeyword implements SchemaKeyword {
    private static final SchemaMapKeyword EMPTY = new SchemaMapKeyword(Collections.emptyMap());
    private final Map<String, Schema> schemas;

    @Builder(toBuilder = true)
    public SchemaMapKeyword(@NonNull @Singular Map<String, Schema> schemas) {
        checkNotNull(schemas, "schemas must not be null");
        this.schemas = ImmutableMap.copyOf(schemas);
    }

    public static SchemaMapKeyword empty() {
        return EMPTY;
    }

    @Override
    public void writeToGenerator(KeywordInfo<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        generator.writeKey(keyword);
        generator.writeStartObject();
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            generator.writeKey(entry.getKey());
            final Schema schema = entry.getValue();
            schema.asVersion(version).toJson(generator);
        }
        generator.writeEnd();
    }

    @Override
    public String toString() {
        return schemas.toString();
    }
}
