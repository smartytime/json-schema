package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.EqualsAndHashCode;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class SchemaListKeyword implements SchemaKeyword {
    private final List<Schema> schemas;

    public SchemaListKeyword(List<Schema> schemas) {
        checkNotNull(schemas, "schemas must not be null");
        this.schemas = schemas;
    }

    @Override
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        generator.writeKey(keyword);
        generator.writeStartArray();
        for (Schema schema : schemas) {
            schema.asVersion(version).toJson(generator);
        }
        generator.writeEnd();
    }

    public List<Schema> getSchemas() {
        return schemas;
    }
}
