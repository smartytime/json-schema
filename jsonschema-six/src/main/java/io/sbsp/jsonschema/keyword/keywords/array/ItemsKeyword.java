package io.sbsp.jsonschema.keyword.keywords.array;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.Collections;
import java.util.Set;

public class ItemsKeyword implements SchemaKeywordBuilder {
    public Set<JsonSchemaKeywordType> getKeywords() {
        return Collections.singleton(JsonSchemaKeywordType.ITEMS);
    }
}