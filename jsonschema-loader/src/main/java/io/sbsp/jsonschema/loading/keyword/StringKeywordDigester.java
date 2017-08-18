package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;

import javax.json.JsonString;
import javax.json.JsonValue;

public class StringKeywordDigester extends BaseKeywordDigester<StringKeyword> {

    public StringKeywordDigester(KeywordInfo<StringKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected StringKeyword extractKeyword(JsonValue jsonValue) {
        final String value = ((JsonString) jsonValue).getString();
        return new StringKeyword(value);
    }
}
