package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;

import static com.google.common.base.Preconditions.checkNotNull;

public class SingleSchemaKeyword extends SchemaKeywordImpl<Schema> {
    public SingleSchemaKeyword(Schema keywordValue) {
        super(keywordValue);
    }

    @Override
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        checkNotNull(version, "version must not be null");
        generator.writeKey(keyword);
        //Make sure the underlying schema is going to serialize json for the correct json-schema version
        value().asVersion(version).toJson(generator);
    }

    public Schema getSchema() {
        return value();
    }
}
