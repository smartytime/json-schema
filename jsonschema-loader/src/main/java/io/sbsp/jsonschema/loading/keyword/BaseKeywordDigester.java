package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.SchemaLoader;
import lombok.Getter;

import javax.json.JsonValue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

public abstract class BaseKeywordDigester<T extends SchemaKeyword> implements KeywordDigester<T> {
    @Getter
    private final KeywordInfo<T> keyword;

    @Getter
    private final List<KeywordInfo<T>> includedKeywords;

    public BaseKeywordDigester(KeywordInfo<T> keyword, JsonValue.ValueType... expectedTypes) {
        this.keyword = checkNotNull(keyword);
        if (expectedTypes != null && expectedTypes.length > 0) {
            this.includedKeywords = keyword.getTypeVariants(expectedTypes);
        } else {
            this.includedKeywords = Collections.singletonList(keyword);
        }
    }

    @Override
    public Optional<KeywordDigest<T>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {
        final JsonValue jsonValue = jsonObject.getOrDefault(keyword.key(), JsonValue.NULL);
        final T schemaKeyword = extractKeyword(jsonValue);
        return Optional.of(schemaKeyword)
                .map(keywordValue -> KeywordDigest.of(keyword, keywordValue));
    }

    protected abstract T extractKeyword(JsonValue jsonValue);
}
