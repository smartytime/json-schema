package io.sbsp.jsonschema.extractor.keyword;

import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.StringSetKeyword;

import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.LinkedHashSet;
import java.util.Set;

public class StringSetKeywordExtractor extends SchemaKeywordExtractorImpl<StringSetKeyword> {

    public StringSetKeywordExtractor(KeywordMetadata<StringSetKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected StringSetKeyword extractValue(JsonValue jsonValue) {

        Set<String> values = new LinkedHashSet<>();
        for (JsonString string : jsonValue.asJsonArray().getValuesAs(JsonString.class)) {
            values.add(string.getString());
        }
        return new StringSetKeyword(values);
    }
}
