package io.sbsp.jsonschema.keyword.keywords.string;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.Collections;
import java.util.Set;

public class PatternKeyword implements SchemaKeywordBuilder {
    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return Collections.singleton(JsonSchemaKeywordType.PATTERN);
    }
}