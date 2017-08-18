package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;

import javax.json.JsonValue;

public class JsonArrayKeywordDigester extends BaseKeywordDigester<JsonArrayKeyword> {

    public JsonArrayKeywordDigester(KeywordInfo<JsonArrayKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected JsonArrayKeyword extractKeyword(JsonValue jsonValue) {
        return new JsonArrayKeyword(jsonValue.asJsonArray());
    }
}
