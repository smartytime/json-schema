package io.sbsp.jsonschema.builder;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class SchemaMapKeywordBuilder implements SchemaKeywordBuilder<SchemaMapKeyword> {

    private final Map<String, SchemaBuilder> schemaMap = new LinkedHashMap<>();

    public Map<String, SchemaBuilder> getSchemas() {
        return schemaMap;
    }

    public SchemaMapKeywordBuilder addSchema(String key, SchemaBuilder anotherValue) {
        checkNotNull(key, "key must not be null");
        checkNotNull(anotherValue, "anotherValue must not be null");
        schemaMap.put(key, anotherValue);
        return this;
    }

    public SchemaMapKeywordBuilder addAllSchemas(Map<String, SchemaBuilder> schemas) {
        checkNotNull(schemas, "schemas must not be null");
        schemaMap.clear();
        schemaMap.putAll(schemas);
        return this;
    }

    @Override
    public Stream<SchemaBuilder> getAllSchemas() {
        return schemaMap.values().stream();
    }

    @Override
    public SchemaMapKeyword build(SchemaLocation parentLocation, KeywordMetadata<?> keyword, LoadingReport report) {
        final SchemaLocation keywordLocation = parentLocation.child(keyword.getKey());

        Map<String, Schema> keywordMap = new LinkedHashMap<>();
        for (Map.Entry<String, SchemaBuilder> entry : schemaMap.entrySet()) {
            final SchemaLocation keyLocation = keywordLocation.child(entry.getKey());
            final SchemaBuilder childBuilder = entry.getValue();

            keywordMap.put(entry.getKey(), childBuilder.build(keyLocation, report));
        }
        return new SchemaMapKeyword(keywordMap);
    }
}
