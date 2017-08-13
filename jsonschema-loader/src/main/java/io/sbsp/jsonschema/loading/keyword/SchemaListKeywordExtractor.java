package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

@Getter
@EqualsAndHashCode
public class SchemaListKeywordExtractor implements SchemaKeywordExtractor {
    private final KeywordMetadata<SchemaListKeyword> keyword;

    public SchemaListKeywordExtractor(KeywordMetadata<SchemaListKeyword> keyword) {
        this.keyword = keyword;
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, LoadingReport report) {

        checkState(schemaFactory != null, "schemaFactory can't be null");
        List<SchemaBuilder> schemas = new ArrayList<>();
        validateArray(keyword, jsonObject, report).ifPresent(schemaArray -> {
            int i = 0;
            for (JsonValue schemaJson : schemaArray) {
                final SchemaLocation childLocation = jsonObject.getLocation().child(keyword.getKey(), i++);
                if (schemaJson.getValueType() != JsonValue.ValueType.OBJECT) {
                    report.error(typeMismatch(keyword, schemaJson, childLocation));
                    continue;
                }
                final JsonValueWithLocation childValue = JsonValueWithLocation.fromJsonValue(schemaJson, childLocation);
                schemas.add(schemaFactory.createSchemaBuilder(childValue, report));
            }

            builder.addOrRemoveSchemaList(keyword, schemas);
        });
        return report;
    }
}
