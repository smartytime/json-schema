package io.sbsp.jsonschema.keyword.keywords.meta;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$ID;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ID;

public class IdKeyword implements SchemaKeywordBuilder {
    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return ImmutableSet.of(ID, $ID);
    }
}
