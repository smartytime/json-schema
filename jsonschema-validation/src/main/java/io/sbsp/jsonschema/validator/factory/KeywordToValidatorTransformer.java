package io.sbsp.jsonschema.validator.factory;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

/**
 * Extracts any necessary validation keywords from a {@link Schema} instance.
 */
public interface KeywordToValidatorTransformer<K extends SchemaKeyword, V extends KeywordValidator<K>> {
    /**
     * Extracts any necessary keyword validators by inspecting the provided schema.
     */
    V getKeywordValidator(Schema schema, KeywordMetadata<? extends K> keywordInfo, K keyword, SchemaValidatorFactory factory);
}
