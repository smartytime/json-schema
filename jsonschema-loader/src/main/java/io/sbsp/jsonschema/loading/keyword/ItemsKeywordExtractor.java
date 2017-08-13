package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.JsonValueWithLocation.ValueType;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ITEMS;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

public class ItemsKeywordExtractor implements SchemaKeywordExtractor {

    @Override
    public KeywordMetadata<?> getKeyword() {
        return Keywords.items;
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, LoadingReport report) {
        jsonObject.findByKey(ITEMS).ifPresent(itemsValue -> {
            final SchemaLocation itemsPath = jsonObject.getLocation().child(ITEMS);
            switch (itemsValue.getValueType()) {
                case OBJECT:
                    builder.allItemSchema(
                            schemaFactory.createSchemaBuilder(itemsValue, itemsPath, report)
                    );
                    break;
                case ARRAY:
                    int idx = 0;
                    for (JsonValue itemValue : itemsValue.asJsonArray()) {
                        final SchemaLocation idxPath = itemsPath.child(idx++);
                        if (itemValue.getValueType() != ValueType.OBJECT) {
                            report.error(typeMismatch(getKeyword(), itemValue, idxPath));
                        } else {
                            builder.itemSchema(
                                    schemaFactory.createSchemaBuilder(itemValue, idxPath, report)
                            );
                        }
                    }
                    break;
                default:
                    report.error(typeMismatch(getKeyword(), itemsValue, itemsPath));
            }
        });
        return report;
    }
}
