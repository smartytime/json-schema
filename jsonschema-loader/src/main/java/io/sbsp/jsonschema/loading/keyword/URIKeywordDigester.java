package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.URIKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;

import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;

public class URIKeywordDigester extends BaseKeywordDigester<URIKeyword> {

    public URIKeywordDigester(KeywordInfo<URIKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected URIKeyword extractKeyword(JsonValue jsonValue) {
        final String uriString = ((JsonString) jsonValue).getString();
        return new URIKeyword(URI.create(uriString));
    }

}
