package io.sbsp.jsonschema.builder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.DependenciesKeyword;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class DependenciesKeywordBuilder implements SchemaKeywordBuilder<DependenciesKeyword> {

    private SetMultimap<String, String> propertyDependencies = HashMultimap.create();
    private final SchemaMapKeywordBuilder dependencySchemas = new SchemaMapKeywordBuilder();

    public DependenciesKeywordBuilder propertyDependency(String ifThisProperty, String thenExpectThisProperty) {
        checkNotNull(thenExpectThisProperty, "thenExpectThisProperty must not be null");
        checkNotNull(ifThisProperty, "ifThisProperty must not be null");
        propertyDependencies.put(ifThisProperty, thenExpectThisProperty);
        return this;
    }

    public Map<String, SchemaBuilder> getSchemas() {
        return dependencySchemas.getSchemas();
    }

    public DependenciesKeywordBuilder addDependencySchema(String key, SchemaBuilder anotherValue) {
        checkNotNull(key, "key must not be null");
        checkNotNull(anotherValue, "anotherValue must not be null");
        dependencySchemas.addSchema(key, anotherValue);
        return this;
    }

    @Override
    public DependenciesKeyword build(SchemaLocation parentLocation, KeywordMetadata keyword, LoadingReport report) {
        final SchemaMapKeyword builtDependencySchemas = dependencySchemas.build(parentLocation, Keywords.dependencies, report);
        return new DependenciesKeyword(builtDependencySchemas, propertyDependencies);
    }

    @Override
    public Stream<SchemaBuilder> getAllSchemas() {
        return dependencySchemas.getAllSchemas();
    }
}
