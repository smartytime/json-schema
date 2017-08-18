package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.BooleanKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;

import javax.json.JsonValue;

public class BooleanKeywordDigester extends BaseKeywordDigester<BooleanKeyword> {
    public BooleanKeywordDigester(KeywordInfo<BooleanKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected BooleanKeyword extractKeyword(JsonValue jsonValue) {
        if (jsonValue == JsonValue.TRUE) {
            return new BooleanKeyword(true);
        } else {
            return new BooleanKeyword(false);
        }
    }
}
