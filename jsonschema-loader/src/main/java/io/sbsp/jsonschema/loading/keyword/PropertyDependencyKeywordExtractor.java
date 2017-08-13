package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.Getter;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Map;

import static io.sbsp.jsonschema.JsonValueWithLocation.fromJsonValue;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DEPENDENCIES;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

@Getter
public class PropertyDependencyKeywordExtractor implements SchemaKeywordExtractor {
    @Override
    public KeywordMetadata<?> getKeyword() {
        return Keywords.dependencies;
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, LoadingReport report) {
        validateType(Keywords.dependencies, jsonObject, report, JsonObject.class).ifPresent(dependencies -> {
            for (Map.Entry<String, JsonValue> dependency : dependencies.entrySet()) {
                final String dependencyKey = dependency.getKey();
                final JsonValue dependencyValue = dependency.getValue();

                switch (dependencyValue.getValueType()) {
                    case OBJECT:
                        final SchemaLocation childLocation = jsonObject.getLocation()
                                .child(Keywords.dependencies.getKey())
                                .child(dependencyKey);
                        final JsonValueWithLocation schemaJson = fromJsonValue(dependencyValue, childLocation);
                        final SchemaBuilder dependencySchema = schemaFactory.createSchemaBuilder(schemaJson, report);
                        builder.schemaDependency(dependencyKey, dependencySchema);
                        break;
                    case ARRAY:

                        dependencyValue.asJsonArray()
                                .getValuesAs(JsonString::getString)
                                .forEach(arrayItem -> builder.propertyDependency(dependencyKey, arrayItem));
                        break;
                    default:
                        final SchemaLocation issueLocation = jsonObject.getLocation().child(DEPENDENCIES);
                        report.error(typeMismatch(getKeyword(), dependencyValue, issueLocation));
                }
            }
        });
        return report;
    }
}
