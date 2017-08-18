package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class SingleSchemaKeywordDigester implements KeywordDigester<SingleSchemaKeyword> {

    @Getter
    private final KeywordInfo<SingleSchemaKeyword> keyword;

    public SingleSchemaKeywordDigester(KeywordInfo<SingleSchemaKeyword> keyword) {
        this.keyword = checkNotNull(keyword);
    }

    @Override
    public List<KeywordInfo<SingleSchemaKeyword>> getIncludedKeywords() {
        return Collections.singletonList(keyword);
    }

    @Override
    public Optional<KeywordDigest<SingleSchemaKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {
        final JsonValueWithPath subschema = jsonObject.path(keyword);
        final Schema schema = schemaLoader.loadSubSchema(subschema, subschema.getRoot(), report);
        return KeywordDigest.ofOptional(keyword, new SingleSchemaKeyword(schema));
    }
}
