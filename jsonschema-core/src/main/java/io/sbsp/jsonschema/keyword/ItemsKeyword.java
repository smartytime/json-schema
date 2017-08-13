package io.sbsp.jsonschema.keyword;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
public class ItemsKeyword implements SchemaKeyword {

    private final List<Schema> indexedSchemas;
    private final Schema additionalItemSchema;
    private final Schema allItemSchema;

    public ItemsKeyword(Schema allItemSchema, Schema additionalItemSchema, List<Schema> schemas) {
        this.allItemSchema = allItemSchema;
        this.indexedSchemas = ImmutableList.copyOf(schemas);
        this.additionalItemSchema = additionalItemSchema;
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

    public boolean hasIndexedSchemas() {
        return !indexedSchemas.isEmpty();
    }

    @Override
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        if (!indexedSchemas.isEmpty()) {
            generator.writeKey(Keywords.items);
            generator.writeStartArray();
            for (Schema schema : indexedSchemas) {
                schema.asVersion(version).toJson(generator);
            }
            generator.writeEnd();
        } else {
            getAllItemSchema().ifPresent(schema -> {
                generator.writeKey(Keywords.items);
                schema.toJson(generator, version);
            });
        }
        getAdditionalItemSchema().ifPresent(schema -> {
            generator.writeKey(Keywords.additionalItems);
            additionalItemSchema.toJson(generator);
        });
    }
}
