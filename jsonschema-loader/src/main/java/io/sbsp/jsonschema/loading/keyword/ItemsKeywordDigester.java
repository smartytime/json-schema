package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;

import javax.json.JsonValue;
import java.util.List;
import java.util.Optional;

import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.OBJECT;

public class ItemsKeywordDigester implements KeywordDigester<ItemsKeyword> {

    @Override
    public List<KeywordInfo<ItemsKeyword>> getIncludedKeywords() {
        return Keywords.ITEMS.getTypeVariants(OBJECT, ARRAY);
    }

    @Override
    public Optional<KeywordDigest<ItemsKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {
        final JsonValueWithPath itemsValue = jsonObject.path(Keywords.ITEMS);
            switch (itemsValue.getValueType()) {
                case OBJECT:
                    builder.allItemSchema(
                            schemaLoader.subSchemaBuilder(itemsValue, itemsValue.getRoot(), report)
                    );
                    break;
                case ARRAY:
                    itemsValue.forEachIndex((idx, idxValue)->{
                        if (idxValue.getValueType() != OBJECT) {
                            report.error(typeMismatch(Keywords.ITEMS, idxValue));
                        } else {
                            builder.itemSchema(
                                    schemaLoader.subSchemaBuilder(idxValue, idxValue.getRoot(), report)
                            );
                        }
                    });
                    break;
                default:
                    report.error(typeMismatch(Keywords.ITEMS, itemsValue));
            }

        return Optional.empty();
    }
}
