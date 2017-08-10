package io.sbsp.jsonschema.keyword;

import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.Getter;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class PropertyDependencyKeyword implements SchemaKeyword {
    private final SetMultimap<String, String> propertyDependencies;
    private final SchemaMapKeyword dependencySchemas;

    public PropertyDependencyKeyword(SchemaMapKeyword dependencySchemas) {
        this.dependencySchemas = checkNotNull(dependencySchemas);
        this.propertyDependencies = null;
    }

    public PropertyDependencyKeyword(SetMultimap<String, String> propertyDependencies) {
        this.propertyDependencies = checkNotNull(propertyDependencies);
        this.dependencySchemas = null;
    }

    @Override
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        if (propertyDependencies.size() > 0) {
            generator.writeKey(dependencies);
            generator.writeStartObject();
            propertyDependencies.asMap().forEach((prop, setOfDependentProps)->{
                generator.writeKey(prop);
                generator.writeStartArray();
                for (String dependentProp : setOfDependentProps) {
                    generator.write(dependentProp);
                }
                generator.writeEnd();
            });
            generator.writeEnd();
        } else {
            dependencySchemas.writeToGenerator(dependencies, generator, version);
        }
    }
}
