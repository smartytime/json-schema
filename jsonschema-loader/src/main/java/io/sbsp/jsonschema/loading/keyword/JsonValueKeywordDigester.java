package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.JsonValueKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;

import javax.json.JsonValue;

public class JsonValueKeywordDigester extends BaseKeywordDigester<JsonValueKeyword> {

    public JsonValueKeywordDigester(KeywordInfo<JsonValueKeyword> keyword, JsonValue.ValueType... acceptedTypes) {
        super(keyword, acceptedTypes);
    }

    @Override
    protected JsonValueKeyword extractKeyword(JsonValue jsonValue) {
        return new JsonValueKeyword(jsonValue);
    }
}
