package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import lombok.Getter;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public abstract class KeywordValidator implements SchemaValidator {
    private final JsonSchemaKeyword keyword;
    protected final Schema schema;

    public KeywordValidator(JsonSchemaKeyword keyword, Schema schema) {
        this.keyword = checkNotNull(keyword);
        this.schema = checkNotNull(schema);
    }
}
