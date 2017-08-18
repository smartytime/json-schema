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
import io.sbsp.jsonschema.utils.Schemas;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.TRUE;

@Getter
@EqualsAndHashCode
public class AdditionalPropertiesBooleanKeywordDigester implements KeywordDigester<SingleSchemaKeyword> {

    private final List<KeywordInfo<SingleSchemaKeyword>> keywords;

    public AdditionalPropertiesBooleanKeywordDigester() {
        this.keywords = Keywords.ADDITIONAL_PROPERTIES.getTypeVariants(TRUE, FALSE);
    }

    @Override
    public List<KeywordInfo<SingleSchemaKeyword>> getIncludedKeywords() {
        return keywords;
    }

    @Override
    public Optional<KeywordDigest<SingleSchemaKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder,  SchemaLoader schemaLoader, LoadingReport report) {
        final JsonValueWithPath additionalProperties = jsonObject.path(Keywords.ADDITIONAL_PROPERTIES);
        if (additionalProperties.is(FALSE)) {
            return KeywordDigest.ofOptional(keywords.get(0), new SingleSchemaKeyword(Schemas.falseSchema()));
        }
        return Optional.empty();
    }
}