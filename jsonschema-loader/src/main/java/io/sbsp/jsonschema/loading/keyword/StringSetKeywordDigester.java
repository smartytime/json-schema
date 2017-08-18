package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.StringSetKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;

import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.LinkedHashSet;
import java.util.Set;

public class StringSetKeywordDigester extends BaseKeywordDigester<StringSetKeyword> {

    public StringSetKeywordDigester(KeywordInfo<StringSetKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected StringSetKeyword extractKeyword(JsonValue jsonValue) {

        Set<String> values = new LinkedHashSet<>();
        for (JsonString string : jsonValue.asJsonArray().getValuesAs(JsonString.class)) {
            values.add(string.getString());
        }
        return new StringSetKeyword(values);
    }
}
