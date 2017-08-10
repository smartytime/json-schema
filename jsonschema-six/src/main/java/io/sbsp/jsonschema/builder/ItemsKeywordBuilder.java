package io.sbsp.jsonschema.builder;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.keyword.ItemsKeyword;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class ItemsKeywordBuilder implements SchemaKeywordBuilder<ItemsKeyword> {

    private final List<JsonSchemaBuilder> indexSchemas;
    private final JsonSchemaBuilder additionalItemSchema;

    public ItemsKeywordBuilder(JsonSchemaBuilder indexSchemas) {
        checkNotNull(indexSchemas, "indexSchemas must not be null");
        this.indexSchemas = Collections.singletonList(indexSchemas);
        this.additionalItemSchema = null;
    }

    public ItemsKeywordBuilder(Collection<? extends JsonSchemaBuilder> keywordValues) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        this.indexSchemas = Collections.unmodifiableList(new ArrayList<>(keywordValues));
        this.additionalItemSchema = null;
    }

    public ItemsKeywordBuilder(JsonSchemaBuilder additionalItemSchema, Collection<? extends JsonSchemaBuilder> keywordValues) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        this.indexSchemas = Collections.unmodifiableList(new ArrayList<>(keywordValues));
        this.additionalItemSchema = additionalItemSchema;
    }

    public List<JsonSchemaBuilder> getSchemas() {
        return indexSchemas;
    }

    public ItemsKeywordBuilder withAnotherSchema(JsonSchemaBuilder anotherValue) {
        checkNotNull(anotherValue, "anotherValue must not be null");
        final List<JsonSchemaBuilder> items = new ArrayList<>(indexSchemas);
        items.add(anotherValue);
        return new ItemsKeywordBuilder(additionalItemSchema, items);
    }

    public ItemsKeywordBuilder withAdditionalItemsSchema(JsonSchemaBuilder additionalItemSchema) {
        return new ItemsKeywordBuilder(additionalItemSchema, indexSchemas);
    }

    @Override
    public ItemsKeyword build(SchemaLocation location, @Nullable SchemaFactory factory, @Nullable JsonObject rootDocument) {

        List<Schema> listOfSchema = new ArrayList<>();
        int i = 0;
        for (JsonSchemaBuilder idxSchemaBuilder : indexSchemas) {
            final SchemaLocation idxLocation = location.child(i++);
            final Schema idxSchema = Optional.ofNullable(factory)
                    .flatMap(f -> f.findCachedSchema(idxLocation.getUniqueURI()))
                    .orElseGet(() -> idxSchemaBuilder.schemaFactory(factory)
                            .currentDocument(rootDocument)
                            .build(idxLocation));
            listOfSchema.add(idxSchema);
        }

        final Schema additionalItemSchema;
        if (this.additionalItemSchema != null) {
            final SchemaLocation additionalItemsLocation = location.child(JsonSchemaKeywordType.ADDITIONAL_ITEMS);
            this.additionalItemSchema.schemaFactory(factory).currentDocument(rootDocument);
            additionalItemSchema = Optional.ofNullable(factory)
                    .flatMap(f -> f.findCachedSchema(additionalItemsLocation.getUniqueURI()))
                    .orElseGet(() -> this.additionalItemSchema.schemaFactory(factory)
                            .currentDocument(rootDocument)
                            .build(additionalItemsLocation));
        } else {
            additionalItemSchema = null;
        }

        return new ItemsKeyword(additionalItemSchema, listOfSchema);
    }
}
