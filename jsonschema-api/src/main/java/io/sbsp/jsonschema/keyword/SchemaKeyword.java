package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;

/**
 * Stores the values of a keyword (or keywords), and can serialize those values to json.
 */
public interface SchemaKeyword {
    void writeJson(KeywordInfo<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version);
}
