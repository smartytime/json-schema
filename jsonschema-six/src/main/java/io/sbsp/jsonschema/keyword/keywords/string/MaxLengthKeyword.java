package io.sbsp.jsonschema.keyword.keywords.string;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.Collections;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_LENGTH;

public class MaxLengthKeyword implements SchemaKeywordBuilder {
    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return Collections.singleton(MAX_LENGTH);
    }
}