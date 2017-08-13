package io.sbsp.jsonschema.validator.factory;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

/**
 * Extracts any necessary validation keywords from a {@link Schema} instance.
 */
@FunctionalInterface
public interface KeywordValidatorCreator<K extends SchemaKeyword, V extends KeywordValidator<K>> {
    /**
     * Extracts any necessary keyword validators by inspecting the provided schema.
     */
    V getKeywordValidator(K keyword, Schema schema, SchemaValidatorFactory factory);
}
