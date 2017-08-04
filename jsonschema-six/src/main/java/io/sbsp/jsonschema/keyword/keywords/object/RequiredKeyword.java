package io.sbsp.jsonschema.keyword.keywords.object;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.EnumSet;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.REQUIRED;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.REQUIRED_PROPERTY;

public class RequiredKeyword implements SchemaKeywordBuilder {

    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return EnumSet.of(REQUIRED, REQUIRED_PROPERTY);
    }
}
