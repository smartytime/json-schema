package io.sbsp.jsonschema.builder;

import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.loading.LoadingReport;

import java.util.stream.Stream;

public interface SchemaKeywordBuilder<K extends SchemaKeyword> {
    K build(SchemaLocation parentLocation, KeywordMetadata<?> keyword, LoadingReport report);
    Stream<SchemaBuilder> getAllSchemas();
}
