package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import lombok.Getter;

import javax.json.JsonObject;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.sbsp.jsonschema.JsonValueWithLocation.*;

public class SingleSchemaKeywordExtractor implements SchemaKeywordExtractor {

    @Getter
    private final KeywordMetadata<SingleSchemaKeyword> keyword;

    public SingleSchemaKeywordExtractor(KeywordMetadata<SingleSchemaKeyword> keyword) {
        this.keyword = checkNotNull(keyword);
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, LoadingReport report) {
        checkState(schemaFactory != null, "schemaExtractor can't be null");
        validateType(keyword, jsonObject, report, JsonObject.class).ifPresent(subschemaJson -> {
            final JsonValueWithLocation subschema = fromJsonValue(subschemaJson, jsonObject.getLocation().child(keyword.getKey()));
            builder.addOrRemoveSchema(keyword, schemaFactory.createSchemaBuilder(subschema, report));
        });

        return report;
    }
}
