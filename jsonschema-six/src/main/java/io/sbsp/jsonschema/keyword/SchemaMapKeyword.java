package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@EqualsAndHashCode
public class SchemaMapKeyword implements SchemaKeyword {
    private final Map<String, Schema> schemas;

    public SchemaMapKeyword(Map<String, Schema> schemas) {
        checkNotNull(schemas, "schemas must not be null");
        this.schemas = Collections.unmodifiableMap(schemas);
    }

    @Override
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        generator.writeKey(keyword);
        generator.writeStartObject();
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            generator.writeKey(entry.getKey());
            final Schema schema = entry.getValue();
            schema.asVersion(version).toJson(generator);
        }
        generator.writeEnd();
    }
}
