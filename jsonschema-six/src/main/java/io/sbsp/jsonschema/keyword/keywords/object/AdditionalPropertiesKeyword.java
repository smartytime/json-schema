package io.sbsp.jsonschema.keyword.keywords.object;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.builder.SchemaKeywordBuilder;

import java.util.EnumSet;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_PROPERTIES_WITH_BOOLEAN;

public class AdditionalPropertiesKeyword implements SchemaKeywordBuilder {
    @Override
    public Set<JsonSchemaKeywordType> getKeywords() {
        return EnumSet.of(ADDITIONAL_PROPERTIES, ADDITIONAL_PROPERTIES_WITH_BOOLEAN);
    }
}
