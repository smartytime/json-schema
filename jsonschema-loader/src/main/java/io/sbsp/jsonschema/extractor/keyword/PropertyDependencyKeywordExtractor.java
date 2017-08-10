package io.sbsp.jsonschema.extractor.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.extractor.ExtractionReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.Getter;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Map;

import static io.sbsp.jsonschema.JsonValueWithLocation.*;

@Getter
public class PropertyDependencyKeywordExtractor implements SchemaKeywordExtractor {
    @Override
    public KeywordMetadata<?> getKeyword() {
        return SchemaKeyword.dependencies;
    }

    @Override
    public ExtractionReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, ExtractionReport report) {
        validateType(SchemaKeyword.dependencies, jsonObject, report, JsonObject.class).ifPresent(dependencies -> {
            for (Map.Entry<String, JsonValue> dependency : dependencies.entrySet()) {
                final String dependencyKey = dependency.getKey();
                final JsonValue dependencyValue = dependency.getValue();

                switch (dependencyValue.getValueType()) {
                    case OBJECT:
                        final SchemaLocation childLocation = jsonObject.getLocation()
                                .child(SchemaKeyword.dependencies.getKey())
                                .child(dependencyKey);
                        final JsonValueWithLocation schemaJson = fromJsonValue(dependencyValue, childLocation);
                        final JsonSchemaBuilder dependencySchema = schemaFactory.createSchemaBuilder(schemaJson, report);
                        builder.schemaDependency(dependencyKey, dependencySchema);
                        break;
                    case ARRAY:

                        dependencyValue.asJsonArray()
                                .getValuesAs(JsonString::getString)
                                .forEach(arrayItem -> builder.propertyDependency(dependencyKey, arrayItem));
                        break;
                    default:
                        report.logTypeMismatch(null, null);
                }
            }
        });
        return report;
    }
}
