package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import lombok.Getter;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public abstract class KeywordValidator implements SchemaValidator {
    private final JsonSchemaKeywordType keyword;
    protected final Schema schema;

    public KeywordValidator(JsonSchemaKeywordType keyword, Schema schema) {
        this.keyword = checkNotNull(keyword);
        this.schema = checkNotNull(schema);
    }
}
