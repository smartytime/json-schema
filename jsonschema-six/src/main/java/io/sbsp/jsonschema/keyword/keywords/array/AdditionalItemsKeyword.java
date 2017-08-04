package io.sbsp.jsonschema.keyword.keywords.array;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.EnumSet;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_ITEMS_WITH_BOOLEAN;

public class AdditionalItemsKeyword implements SchemaKeywordBuilder {

    public Set<JsonSchemaKeywordType> getKeywords() {
        return EnumSet.of(ADDITIONAL_ITEMS, ADDITIONAL_ITEMS_WITH_BOOLEAN);
    }
}