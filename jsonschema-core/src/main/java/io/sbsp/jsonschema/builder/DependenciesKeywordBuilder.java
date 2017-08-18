package io.sbsp.jsonschema.builder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword.SchemaMapKeywordBuilder;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.DependenciesKeyword;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class DependenciesKeywordBuilder {

    private final SetMultimap<String, String> propertyDependencies;
    private final SchemaMapKeywordBuilder dependencySchemas;

    public DependenciesKeywordBuilder() {
        propertyDependencies = HashMultimap.create();
        dependencySchemas = SchemaMapKeyword.builder();
    }

    public DependenciesKeywordBuilder(SetMultimap<String, String> propertyDependencies, SchemaMapKeyword dependencySchemas) {
        checkNotNull(propertyDependencies, "propertyDependencies must not be null");
        checkNotNull(dependencySchemas, "dependencySchemas must not be null");

        this.propertyDependencies = HashMultimap.create(propertyDependencies);
        this.dependencySchemas = dependencySchemas.toBuilder();
    }

    public DependenciesKeywordBuilder(DependenciesKeyword keyword) {
        this.propertyDependencies = HashMultimap.create(keyword.getPropertyDependencies());
        this.dependencySchemas = keyword.getDependencySchemas().toBuilder();
    }

    public DependenciesKeywordBuilder propertyDependency(String ifThisProperty, String thenExpectThisProperty) {
        checkNotNull(thenExpectThisProperty, "thenExpectThisProperty must not be null");
        checkNotNull(ifThisProperty, "ifThisProperty must not be null");
        propertyDependencies.put(ifThisProperty, thenExpectThisProperty);
        return this;
    }

    public DependenciesKeywordBuilder addDependencySchema(String key, Schema anotherValue) {
        checkNotNull(key, "key must not be null");
        checkNotNull(anotherValue, "anotherValue must not be null");
        dependencySchemas.schema(key, anotherValue);
        return this;
    }

    public DependenciesKeyword build() {
        return new DependenciesKeyword(dependencySchemas.build(), propertyDependencies);
    }
}
