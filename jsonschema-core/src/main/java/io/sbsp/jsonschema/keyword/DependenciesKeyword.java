package io.sbsp.jsonschema.keyword;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@EqualsAndHashCode
public class DependenciesKeyword implements SchemaKeyword {
    private final SetMultimap<String, String> propertyDependencies;
    private final SchemaMapKeyword dependencySchemas;

    public DependenciesKeyword(SchemaMapKeyword dependencySchemas, SetMultimap<String, String> propertyDependencies) {
        checkNotNull(propertyDependencies, "propertyDependencies must not be null");
        checkNotNull(dependencySchemas, "dependencySchemas must not be null");
        this.propertyDependencies = ImmutableSetMultimap.copyOf(propertyDependencies);
        this.dependencySchemas = dependencySchemas;
    }

    @Override
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        generator.writeKey(Keywords.dependencies);
        generator.writeStartObject();

        propertyDependencies.asMap().forEach((prop, setOfDependentProps) -> {
            generator.writeKey(prop);
            generator.writeStartArray();
            for (String dependentProp : setOfDependentProps) {
                generator.write(dependentProp);
            }
            generator.writeEnd();
        });

        dependencySchemas.getSchemas().forEach((key, schema)-> {
            generator.writeKey(key);
            schema.asVersion(version).toJson(generator);
        });
        generator.writeEnd();
    }
}
