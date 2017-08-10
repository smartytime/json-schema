package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
public class ItemsKeyword implements SchemaKeyword {

    private final List<Schema> indexedSchemas;
    private final Schema additionalItemSchema;
    private final Schema allItemSchema;

    public ItemsKeyword(Schema additionalItemSchema, List<Schema> schemas) {
        if (schemas.size() == 1) {
            this.allItemSchema = schemas.get(0);
            this.indexedSchemas = Collections.emptyList();
        } else {
            this.indexedSchemas = schemas;
            this.allItemSchema = null;
        }
        this.additionalItemSchema = additionalItemSchema;
    }

    @Override
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        generator.writeKey(items);
        if (hasIndexedSchemas()) {
            generator.writeStartArray();
            for (Schema schema : indexedSchemas) {
                schema.asVersion(version).toJson(generator);
            }
            generator.writeEnd();
        } else {
            getAllItemSchema().ifPresent(schema -> schema.toJson(generator, version));
        }
    }

    public boolean hasIndexedSchemas() {
        return allItemSchema == null;
    }

    public List<Schema> getIndexedSchemas() {
        return indexedSchemas;
    }

    public Optional<Schema> getAllItemSchema() {
        return Optional.ofNullable(allItemSchema);
    }

    public Optional<Schema> getAdditionalItemSchema() {
        return Optional.ofNullable(additionalItemSchema);
    }
}
