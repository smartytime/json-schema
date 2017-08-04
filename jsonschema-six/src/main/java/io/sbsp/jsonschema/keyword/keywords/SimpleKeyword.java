package io.sbsp.jsonschema.keyword.keywords;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimpleKeyword<T> implements SchemaKeyword {
    private final T keywordValue;
    private final Set<JsonSchemaKeywordType> keywords;

    public SimpleKeyword(T keywordValue, JsonSchemaKeywordType keyword) {
        checkNotNull(keywordValue, "keywordValue must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword));
        this.keywordValue = keywordValue;
    }

    public SimpleKeyword(T keywordValue, JsonSchemaKeywordType keyword, JsonSchemaKeywordType... addtlKeywords) {
        checkNotNull(keywordValue, "keywordValue must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword, addtlKeywords));
        this.keywordValue = keywordValue;
    }

    public T getKeywordValue() {
        return keywordValue;
    }

}
