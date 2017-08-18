package io.sbsp.jsonschema.keyword;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import io.sbsp.jsonschema.utils.Schemas;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
public class ItemsKeyword implements SchemaKeyword {

    private final List<Schema> indexedSchemas;
    private final Schema additionalItemSchema;
    private final Schema allItemSchema;

    @Builder(toBuilder = true)
    private ItemsKeyword(Schema allItemSchema, Schema additionalItemSchema, @Singular @NonNull List<Schema> indexedSchemas) {
        this.allItemSchema = allItemSchema;
        this.indexedSchemas = ImmutableList.copyOf(indexedSchemas);
        this.additionalItemSchema = additionalItemSchema;
    }

    public static ItemsKeyword newInstance() {
        return builder().build();
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
    public void writeToGenerator(KeywordInfo<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        if (!indexedSchemas.isEmpty()) {
            generator.writeKey(Keywords.ITEMS);
            generator.writeStartArray();
            for (Schema schema : indexedSchemas) {
                schema.asVersion(version).toJson(generator);
            }
            generator.writeEnd();
        } else {
            getAllItemSchema().ifPresent(schema -> {
                generator.writeKey(Keywords.ITEMS);
                schema.toJson(generator, version);
            });
        }
        getAdditionalItemSchema().ifPresent(schema -> {
            if (version.isBefore(JsonSchemaVersion.Draft6) && Schemas.falseSchema().equals(additionalItemSchema)) {
                generator.write(Keywords.ADDITIONAL_ITEMS.key(), false);
            } else {
                generator.writeKey(Keywords.ADDITIONAL_ITEMS);
                additionalItemSchema.toJson(generator);
            }
        });
    }
}
