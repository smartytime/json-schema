package io.sbsp.jsonschema.keyword.keywords.shared;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.EnumSet;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ONE_OF;

public class OneOfKeyword implements SchemaKeywordBuilder {
    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return EnumSet.of(ONE_OF);
    }
}
