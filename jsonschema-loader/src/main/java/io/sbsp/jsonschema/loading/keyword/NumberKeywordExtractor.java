package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.NumberKeyword;

import javax.json.JsonNumber;
import javax.json.JsonValue;

public class NumberKeywordExtractor extends SchemaKeywordExtractorImpl<NumberKeyword> {

    public NumberKeywordExtractor(KeywordMetadata<NumberKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected NumberKeyword extractValue(JsonValue jsonValue) {
        return new NumberKeyword(((JsonNumber)jsonValue).doubleValue());
    }


}
