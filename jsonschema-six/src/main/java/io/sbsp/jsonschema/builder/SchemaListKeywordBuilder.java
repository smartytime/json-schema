package io.sbsp.jsonschema.builder;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchemaListKeywordBuilder implements SchemaKeywordBuilder {

    private final List<JsonSchemaBuilder> keywordValue;
    private final Set<JsonSchemaKeywordType> keywords;

    public SchemaListKeywordBuilder(JsonSchemaBuilder keywordValue, JsonSchemaKeywordType keyword) {
        checkNotNull(keywordValue, "keywordValue must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword));
        this.keywordValue = Collections.singletonList(keywordValue);
    }

    public SchemaListKeywordBuilder(JsonSchemaBuilder keywordValue, JsonSchemaKeywordType keyword, JsonSchemaKeywordType... addtlKeywords) {
        checkNotNull(keywordValue, "keywordValue must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword, addtlKeywords));
        this.keywordValue = Collections.singletonList(keywordValue);
    }

    private SchemaListKeywordBuilder(List<JsonSchemaBuilder> keywordValues, Set<JsonSchemaKeywordType> keywords) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        checkNotNull(keywords, "keyword must not be null");
        this.keywords = keywords;
        this.keywordValue = Collections.unmodifiableList(keywordValues);
    }

    public SchemaListKeywordBuilder(Collection<JsonSchemaBuilder> keywordValues, JsonSchemaKeywordType keyword) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword, keyword));
        this.keywordValue = Collections.unmodifiableList(new ArrayList<>(keywordValues));
    }

    public List<JsonSchemaBuilder> getSchemas() {
        return keywordValue;
    }

    public SchemaListKeywordBuilder withAnotherSchema(JsonSchemaBuilder anotherValue) {
        checkNotNull(anotherValue, "anotherValue must not be null");
        final List<JsonSchemaBuilder> items = new ArrayList<>(keywordValue);
        items.add(anotherValue);
        return new SchemaListKeywordBuilder(items, keywords);
    }
}
