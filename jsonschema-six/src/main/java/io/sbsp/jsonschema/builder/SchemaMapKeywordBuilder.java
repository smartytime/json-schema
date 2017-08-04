package io.sbsp.jsonschema.builder;

import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchemaMapKeywordBuilder implements SchemaKeywordBuilder {

    private final Map<String, JsonSchemaBuilder> keywordValue;
    private final Set<JsonSchemaKeywordType> keywords;

    public SchemaMapKeywordBuilder(String key, JsonSchemaBuilder keywordValue, JsonSchemaKeywordType keyword) {
        checkNotNull(key, "key must not be null");
        checkNotNull(keywordValue, "keywordValue must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword));
        this.keywordValue = ImmutableMap.of(key, keywordValue);
    }

    private SchemaMapKeywordBuilder(Map<String, JsonSchemaBuilder> keywordValues, Set<JsonSchemaKeywordType> keywords) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        checkNotNull(keywords, "keyword must not be null");
        this.keywords = keywords;
        this.keywordValue = Collections.unmodifiableMap(keywordValues);
    }

    public SchemaMapKeywordBuilder(Map<String, JsonSchemaBuilder> keywordValues, JsonSchemaKeywordType keyword) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword, keyword));
        this.keywordValue = Collections.unmodifiableMap(keywordValues);
    }

    public Map<String, JsonSchemaBuilder> getSchemas() {
        return keywordValue;
    }

    public SchemaMapKeywordBuilder addSchema(String key, JsonSchemaBuilder anotherValue) {
        checkNotNull(anotherValue, "anotherValue must not be null");
        final Map<String, JsonSchemaBuilder> items = new LinkedHashMap<String, JsonSchemaBuilder>(keywordValue);
        items.put(key, anotherValue);
        return new SchemaMapKeywordBuilder(items, keywords);
    }
}
