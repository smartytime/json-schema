package io.sbsp.jsonschema.validator.extractors;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;

import javax.json.JsonValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Extracts any necessary validation keywords from a {@link Schema} instance.
 */
public interface KeywordValidatorExtractor {

    /**
     * What type of json object applies to the keywords this instance will be extracting.
     *
     * @return A set of JsonValue.ValueType enum constants
     */
    default Set<JsonValue.ValueType> getApplicableTypes() {
        return new HashSet<>(Arrays.asList(JsonValue.ValueType.values()));
    }

    /**
     * Whether or not this extractor should run against a given schema.
     */
    default boolean appliesToSchema(Schema schema) {
        return true;
    }

    /**
     * Extracts any necessary keyword validators by inspecting the provided schema.
     */
    KeywordValidators getKeywordValidators(Schema schema, SchemaValidatorFactory factory);
}
