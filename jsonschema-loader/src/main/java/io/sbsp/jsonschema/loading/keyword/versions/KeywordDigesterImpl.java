package io.sbsp.jsonschema.loading.keyword.versions;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import io.sbsp.jsonschema.loading.keyword.BaseKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.JsonValueToKeywordTransformer;

import javax.json.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class KeywordDigesterImpl<T extends SchemaKeyword> implements KeywordDigester<T> {

    private final JsonValueToKeywordTransformer<T> tx;
    private final KeywordInfo<T> keyword;

    public KeywordDigesterImpl(KeywordInfo<T> keyword, JsonValueToKeywordTransformer<T> tx) {
        this.tx = checkNotNull(tx);
        this.keyword = keyword;
    }

    public KeywordInfo<T> getKeyword() {
        return keyword;
    }

    @Override
    public List<KeywordInfo<T>> getIncludedKeywords() {
        return Collections.singletonList(keyword);
    }

    @Override
    public Optional<KeywordDigest<T>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {
        return tx.loadKeywordFromJsonValue(jsonObject.getWrapped()).map(k -> KeywordDigest.of(keyword, k));
    }
}
