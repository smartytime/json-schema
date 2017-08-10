package io.sbsp.jsonschema.extractor;

import io.sbsp.jsonschema.keyword.KeywordMetadata;

import javax.json.JsonValue;

public class ExtractionReport {
    public ExtractionReport logMissingKeyword(KeywordMetadata<?> foundKeyword, KeywordMetadata<?> expectedKeyword) {
        return this;
    }

    public ExtractionReport logTypeMismatch(KeywordMetadata<?> keyword, JsonValue value) {
        return this;

    }
}
