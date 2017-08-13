package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.JsonValueKeyword;
import io.sbsp.jsonschema.keyword.KeywordMetadata;

import javax.json.JsonValue;

public class JsonValueKeywordExtractor extends SchemaKeywordExtractorImpl<JsonValueKeyword> {
    public JsonValueKeywordExtractor(KeywordMetadata<JsonValueKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected JsonValueKeyword extractValue(JsonValue jsonValue) {
        return new JsonValueKeyword(jsonValue);
    }
}
