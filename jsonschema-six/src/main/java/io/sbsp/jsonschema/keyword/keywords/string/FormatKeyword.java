package io.sbsp.jsonschema.keyword.keywords.string;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.Collections;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.*;

public class FormatKeyword implements SchemaKeywordBuilder {
    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return Collections.singleton(FORMAT);
    }
}