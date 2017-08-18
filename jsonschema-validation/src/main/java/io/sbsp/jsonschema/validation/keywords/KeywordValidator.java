package io.sbsp.jsonschema.validation.keywords;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.ValidationError;
import io.sbsp.jsonschema.validation.ValidationErrorHelper;
import lombok.Getter;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public abstract class KeywordValidator<K extends SchemaKeyword> implements SchemaValidator {
    protected final Schema schema;
    private KeywordInfo<K> keyword;

    public KeywordValidator(KeywordInfo<K> keyword, Schema schema) {
        this.schema = checkNotNull(schema);
        this.keyword = checkNotNull(keyword);
    }

    protected ValidationError.ValidationErrorBuilder buildKeywordFailure(JsonValueWithPath location) {
        return ValidationErrorHelper.buildKeywordFailure(location, schema, keyword);
    }
}
