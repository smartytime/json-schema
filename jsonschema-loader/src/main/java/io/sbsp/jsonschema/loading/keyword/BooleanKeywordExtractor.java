package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.BooleanKeyword;
import io.sbsp.jsonschema.keyword.KeywordMetadata;

import javax.json.JsonValue;

public class BooleanKeywordExtractor extends SchemaKeywordExtractorImpl<BooleanKeyword> {
    public BooleanKeywordExtractor(KeywordMetadata<BooleanKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected BooleanKeyword extractValue(JsonValue jsonValue) {
        if (jsonValue == JsonValue.TRUE) {
            return new BooleanKeyword(true);
        } else {
            return new BooleanKeyword(false);
        }
    }
}
