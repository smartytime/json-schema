package io.sbsp.jsonschema.keyword.keywords.number;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DIVISIBLE_BY;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MULTIPLE_OF;

public class MultipleOfKeyword implements SchemaKeywordBuilder {
    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return ImmutableSet.of(MULTIPLE_OF, DIVISIBLE_BY);
    }
}