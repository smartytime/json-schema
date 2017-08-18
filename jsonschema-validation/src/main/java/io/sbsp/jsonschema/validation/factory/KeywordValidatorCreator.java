package io.sbsp.jsonschema.validation.factory;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

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
