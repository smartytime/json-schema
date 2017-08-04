package io.sbsp.jsonschema.keyword.keywords;

import io.sbsp.jsonschema.JsonSchema;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@EqualsAndHashCode
public class SchemaMapKeyword implements SchemaKeyword {
    private final Map<String, JsonSchema> schemas;
    private final JsonSchemaKeywordType keyword;

    public SchemaMapKeyword(JsonSchemaKeywordType keyword, Map<String, JsonSchema> schemas) {
        checkNotNull(schemas, "schemas must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keyword = keyword;
        this.schemas = schemas;
    }
}
