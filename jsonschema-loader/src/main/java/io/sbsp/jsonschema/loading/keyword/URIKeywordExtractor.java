package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.URIKeyword;

import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;

public class URIKeywordExtractor extends SchemaKeywordExtractorImpl<URIKeyword> {

    public URIKeywordExtractor(KeywordMetadata<URIKeyword> keyword) {
        super(keyword);
    }

    @Override
    protected URIKeyword extractValue(JsonValue jsonValue) {
        final String uriString = ((JsonString) jsonValue).getString();
        return new URIKeyword(URI.create(uriString));
    }

}
