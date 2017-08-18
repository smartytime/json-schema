package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;

import javax.json.JsonNumber;
import javax.json.JsonValue;

public class NumberKeywordDigester extends BaseKeywordDigester<NumberKeyword> {

    public NumberKeywordDigester(KeywordInfo<NumberKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected NumberKeyword extractKeyword(JsonValue jsonValue) {
        return new NumberKeyword(((JsonNumber)jsonValue).doubleValue());
    }


}
