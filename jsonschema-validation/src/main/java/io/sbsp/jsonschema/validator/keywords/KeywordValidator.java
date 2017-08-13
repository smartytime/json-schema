package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import lombok.Getter;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public abstract class KeywordValidator<K extends SchemaKeyword> implements SchemaValidator {
    protected final Schema schema;
    private KeywordMetadata<K> keyword;

    public KeywordValidator(KeywordMetadata<K> keyword, Schema schema) {
        this.schema = checkNotNull(schema);
        this.keyword = checkNotNull(keyword);
    }
}
