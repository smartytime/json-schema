package io.sbsp.jsonschema.extractor.keyword;

import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.StringKeyword;

import javax.json.JsonString;
import javax.json.JsonValue;

public class StringKeywordExtractor extends SchemaKeywordExtractorImpl<StringKeyword> {

    public StringKeywordExtractor(KeywordMetadata<StringKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected StringKeyword extractValue(JsonValue jsonValue) {
        final String value = ((JsonString) jsonValue).getString();
        return new StringKeyword(value);
    }
}
