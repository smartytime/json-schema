package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingIssues;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class MapKeywordDigester implements KeywordDigester<SchemaMapKeyword> {

    @Getter
    private final KeywordInfo<SchemaMapKeyword> keyword;

    public MapKeywordDigester(KeywordInfo<SchemaMapKeyword> keyword) {
        this.keyword = checkNotNull(keyword, "keyword must not be null");
    }

    @Override
    public List<KeywordInfo<SchemaMapKeyword>> getIncludedKeywords() {
        return Collections.singletonList(keyword);
    }

    @Override
    public Optional<KeywordDigest<SchemaMapKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {
        Map<String, Schema> keyedSchemas = new LinkedHashMap<>();
        final JsonValueWithPath propObject = jsonObject.path(keyword);
        propObject.forEachKey((key, value) -> {
            if (value.getValueType() != JsonValue.ValueType.OBJECT) {
                report.error(LoadingIssues.typeMismatch(keyword, value));
            } else {
                keyedSchemas.put(key, schemaLoader.loadSubSchema(value, value.getRoot(), report));
            }
        });
        return KeywordDigest.ofOptional(keyword, new SchemaMapKeyword(keyedSchemas));
    }
}
