package io.sbsp.jsonschema.loading.keyword.versions.flex;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@EqualsAndHashCode
public class AdditionalPropertiesKeywordDigester implements KeywordDigester<SingleSchemaKeyword> {

    @Override
    public List<KeywordInfo<SingleSchemaKeyword>> getIncludedKeywords() {
        return Collections.singletonList(Keywords.ADDITIONAL_PROPERTIES);
    }

    @Override
    public Optional<KeywordDigest<SingleSchemaKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {
        final JsonValueWithPath additionalProperties = jsonObject.path(Keywords.ADDITIONAL_PROPERTIES);
        final SingleSchemaKeyword keywordValue = new SingleSchemaKeyword(schemaLoader.loadSubSchema(additionalProperties, jsonObject.getRoot(), report));
        return KeywordDigest.ofOptional(Keywords.ADDITIONAL_PROPERTIES, keywordValue);
    }

}