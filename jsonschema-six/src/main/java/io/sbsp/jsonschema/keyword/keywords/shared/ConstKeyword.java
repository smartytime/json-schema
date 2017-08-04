package io.sbsp.jsonschema.keyword.keywords.shared;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.EnumSet;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONST;

public class ConstKeyword implements SchemaKeywordBuilder {

    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return EnumSet.of(CONST);
    }
}
