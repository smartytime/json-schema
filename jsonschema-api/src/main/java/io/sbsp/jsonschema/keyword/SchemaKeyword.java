package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;

public interface SchemaKeyword {
    void writeToGenerator(KeywordInfo<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version);
}
