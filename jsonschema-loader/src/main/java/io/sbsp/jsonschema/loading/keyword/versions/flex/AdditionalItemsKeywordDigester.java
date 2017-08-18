package io.sbsp.jsonschema.loading.keyword.versions.flex;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

import static javax.json.JsonValue.ValueType.*;

@Getter
@EqualsAndHashCode
public class AdditionalItemsKeywordDigester implements KeywordDigester<ItemsKeyword> {

    @Override
    public List<KeywordInfo<ItemsKeyword>> getIncludedKeywords() {
        return Keywords.ADDITIONAL_ITEMS.getTypeVariants(OBJECT);
    }

    @Override
    public Optional<KeywordDigest<ItemsKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {

        final JsonValueWithPath additionalItems = jsonObject.path(Keywords.ADDITIONAL_ITEMS);
        final SchemaBuilder addtlItemsBuilder = schemaLoader.subSchemaBuilder(additionalItems, additionalItems.getRoot(), report);
        builder.schemaOfAdditionalItems(addtlItemsBuilder);

        // empty because we added the vlaue to the builder manually
        return Optional.empty();
    }
}