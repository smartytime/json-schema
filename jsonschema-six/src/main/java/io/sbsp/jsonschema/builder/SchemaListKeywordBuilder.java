package io.sbsp.jsonschema.builder;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchemaListKeywordBuilder implements SchemaKeywordBuilder<SchemaListKeyword> {

    private final List<JsonSchemaBuilder> indexSchemas;
    private final Set<JsonSchemaKeywordType> keywords;

    public SchemaListKeywordBuilder(JsonSchemaBuilder indexSchemas) {
        checkNotNull(indexSchemas, "indexSchemas must not be null");
        this.keywords = Collections.emptySet();
        this.indexSchemas = Collections.singletonList(indexSchemas);
    }

    public SchemaListKeywordBuilder(JsonSchemaBuilder indexSchemas, JsonSchemaKeywordType keyword) {
        checkNotNull(indexSchemas, "indexSchemas must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword));
        this.indexSchemas = Collections.singletonList(indexSchemas);
    }

    public SchemaListKeywordBuilder(JsonSchemaBuilder indexSchemas, JsonSchemaKeywordType keyword, JsonSchemaKeywordType... addtlKeywords) {
        checkNotNull(indexSchemas, "indexSchemas must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword, addtlKeywords));
        this.indexSchemas = Collections.singletonList(indexSchemas);
    }

    private SchemaListKeywordBuilder(List<JsonSchemaBuilder> keywordValues, Set<JsonSchemaKeywordType> keywords) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        checkNotNull(keywords, "keyword must not be null");
        this.keywords = keywords;
        this.indexSchemas = Collections.unmodifiableList(keywordValues);
    }

    public SchemaListKeywordBuilder(Collection<? extends JsonSchemaBuilder> keywordValues) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        this.keywords = Collections.emptySet();
        this.indexSchemas = Collections.unmodifiableList(new ArrayList<>(keywordValues));
    }
    public SchemaListKeywordBuilder(Collection<JsonSchemaBuilder> keywordValues, JsonSchemaKeywordType keyword) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword, keyword));
        this.indexSchemas = Collections.unmodifiableList(new ArrayList<>(keywordValues));
    }

    public List<JsonSchemaBuilder> getSchemas() {
        return indexSchemas;
    }

    public SchemaListKeywordBuilder withAnotherSchema(JsonSchemaBuilder anotherValue) {
        checkNotNull(anotherValue, "anotherValue must not be null");
        final List<JsonSchemaBuilder> items = new ArrayList<>(indexSchemas);
        items.add(anotherValue);
        return new SchemaListKeywordBuilder(items, keywords);
    }

    @Override
    public SchemaListKeyword build(SchemaLocation location, @Nullable SchemaFactory factory, @Nullable JsonObject rootDocument) {

        List<Schema> listOfSchema = new ArrayList<>();
        int i = 0;
        for (JsonSchemaBuilder builder : indexSchemas) {
            final SchemaLocation idxLocation = location.child(i++);
            if (factory != null) {
                final Optional<Schema> cachedSchema = factory.findCachedSchema(location.getUniqueURI());
                if (cachedSchema.isPresent()) {
                    listOfSchema.add(cachedSchema.get());
                    continue;
                }
            }
            listOfSchema.add(builder.schemaFactory(factory)
                    .currentDocument(rootDocument)
                    .build(idxLocation))
            ;

        }
        return new SchemaListKeyword(listOfSchema);
    }
}
