package io.sbsp.jsonschema.builder;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.PropertyDependencyKeyword;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class PropertyDependencyKeywordBuilder implements SchemaKeywordBuilder {

    private final SetMultimap<String, String> propertyDependencies;
    private final SchemaMapKeywordBuilder dependencySchemas;

    public PropertyDependencyKeywordBuilder() {
        this.propertyDependencies = ImmutableSetMultimap.of();
        this.dependencySchemas = new SchemaMapKeywordBuilder(Collections.emptyMap());
    }

    public PropertyDependencyKeywordBuilder(String key, String requiredProperties) {
        checkNotNull(key, "key must not be null");
        checkNotNull(requiredProperties, "requiredProperties must not be null");
        this.propertyDependencies = ImmutableSetMultimap.of(key, requiredProperties);
        this.dependencySchemas = new SchemaMapKeywordBuilder(Collections.emptyMap());
    }

    public PropertyDependencyKeywordBuilder(SchemaMapKeywordBuilder schemaDependencies) {
        checkNotNull(schemaDependencies, "schemaDependencies must not be null");
        this.dependencySchemas = schemaDependencies;
        this.propertyDependencies = ImmutableSetMultimap.of();
    }

    private PropertyDependencyKeywordBuilder(SetMultimap<String, String> propertyDependencies, SchemaMapKeywordBuilder builder) {
        checkNotNull(propertyDependencies, "propertyDependencies must not be null");
        this.propertyDependencies = ImmutableSetMultimap.copyOf(propertyDependencies);
        this.dependencySchemas = checkNotNull(builder, "builder must not be null");
    }

    public SetMultimap<String, String> getPropertyDependencies() {
        return propertyDependencies;
    }

    public PropertyDependencyKeywordBuilder propertyDependency(String ifThisProperty, String thenExpectThisProperty) {
        checkNotNull(thenExpectThisProperty, "thenExpectThisProperty must not be null");

        final ImmutableSetMultimap<String, String> withNewValue = ImmutableSetMultimap.<String, String>builder()
                .putAll(propertyDependencies)
                .put(ifThisProperty, thenExpectThisProperty).build();
        return new PropertyDependencyKeywordBuilder(withNewValue, dependencySchemas);
    }

    public Map<String, JsonSchemaBuilder> getSchemas() {
        return dependencySchemas.getSchemas();
    }

    public PropertyDependencyKeywordBuilder addSchema(String key, JsonSchemaBuilder anotherValue) {
        final SchemaMapKeywordBuilder withNewSchema = dependencySchemas.addSchema(key, anotherValue);
        return new PropertyDependencyKeywordBuilder(propertyDependencies, withNewSchema);
    }

    @Override
    public SchemaKeyword build(SchemaLocation location, @Nullable SchemaFactory factory, @Nullable JsonObject rootDocument) {
        if (propertyDependencies.size() > 0) {
            return new PropertyDependencyKeyword(propertyDependencies);
        } else {
            return new PropertyDependencyKeyword(dependencySchemas.build(location, factory, rootDocument));
        }
    }
}
