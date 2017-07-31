package io.dugnutt.jsonschema.validator.keywords;

import io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
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
