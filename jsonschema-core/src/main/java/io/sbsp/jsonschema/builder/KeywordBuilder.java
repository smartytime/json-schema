package io.sbsp.jsonschema.builder;

import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.loading.LoadingReport;

import java.util.stream.Stream;

public interface KeywordBuilder<K extends SchemaKeyword> {
    K build(SchemaLocation parentLocation, KeywordInfo<K> keyword, LoadingReport report);
}
