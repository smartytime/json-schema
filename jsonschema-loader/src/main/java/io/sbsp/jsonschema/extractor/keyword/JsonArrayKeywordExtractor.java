package io.sbsp.jsonschema.extractor.keyword;

import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.KeywordMetadata;

import javax.json.JsonValue;

public class JsonArrayKeywordExtractor extends SchemaKeywordExtractorImpl<JsonArrayKeyword> {

    public JsonArrayKeywordExtractor(KeywordMetadata<JsonArrayKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected JsonArrayKeyword extractValue(JsonValue jsonValue) {
        return new JsonArrayKeyword(jsonValue.asJsonArray());
    }
}
