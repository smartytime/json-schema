package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;

public class BooleanKeyword extends SchemaKeywordImpl<Boolean> {
    public BooleanKeyword(Boolean keywordValue, JsonSchemaKeywordType keyword) {
        super(keywordValue);
    }

    public BooleanKeyword(Boolean keywordValue) {
        super(keywordValue);
    }
}
