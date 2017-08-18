package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

@Getter
@EqualsAndHashCode
public class ListKeywordDigester implements KeywordDigester<SchemaListKeyword> {
    private final KeywordInfo<SchemaListKeyword> keyword;

    public ListKeywordDigester(KeywordInfo<SchemaListKeyword> keyword) {
        this.keyword = keyword;
    }

    @Override
    public List<KeywordInfo<SchemaListKeyword>> getIncludedKeywords() {
        return Collections.singletonList(keyword);
    }

    @Override
    public Optional<KeywordDigest<SchemaListKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {
        List<Schema> schemas = new ArrayList<>();
        final JsonValueWithPath jsonArray = jsonObject.path(keyword);
        jsonArray.forEachIndex((idx, item) -> {
            if (item.getValueType() != JsonValue.ValueType.OBJECT) {
                report.error(typeMismatch(keyword, item));
            } else {
                schemas.add(schemaLoader.loadSubSchema(item, item.getRoot(), report));
            }
        });

        return KeywordDigest.ofOptional(keyword, new SchemaListKeyword(schemas));
    }
}
