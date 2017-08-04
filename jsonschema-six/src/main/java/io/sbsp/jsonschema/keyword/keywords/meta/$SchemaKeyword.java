package io.sbsp.jsonschema.keyword.keywords.meta;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.Collections;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$SCHEMA;

public class $SchemaKeyword implements SchemaKeywordBuilder {
    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return Collections.singleton($SCHEMA);
    }

}
