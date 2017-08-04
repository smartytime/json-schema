package io.sbsp.jsonschema.keyword.keywords;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@EqualsAndHashCode
public class SchemaListKeyword implements SchemaKeyword {
    private final List<Schema> schemas;
    private final JsonSchemaKeywordType keyword;

    public SchemaListKeyword(JsonSchemaKeywordType keyword, List<Schema> schemas) {
        checkNotNull(schemas, "schemas must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keyword = keyword;
        this.schemas = schemas;
    }
}
