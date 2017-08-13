package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.Getter;

import javax.json.JsonValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

public abstract class SchemaKeywordExtractorImpl<T extends SchemaKeyword> implements SchemaKeywordExtractor {
    @Getter
    private final KeywordMetadata<T> keyword;

    public SchemaKeywordExtractorImpl(KeywordMetadata<T> keyword) {
        this.keyword = keyword;
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, LoadingReport report) {
        final JsonValue jsonValue = jsonObject.getOrDefault(keyword.getKey(), JsonValue.NULL);
        if (jsonValue != JsonValue.NULL) {
            //Verify the correct type
            if (!this.verifyType(jsonValue)) {
                final SchemaLocation location = jsonObject.getLocation().child(keyword.getKey());
                return report.error(typeMismatch(keyword, jsonValue, location));
            }

            final T keywordValue = extractValue(jsonValue);
            builder.keyword(keyword, keywordValue);
        }

        return report;
    }

    protected abstract T extractValue(JsonValue jsonValue);

    private boolean verifyType(JsonValue jsonValue) {
        checkNotNull(jsonValue, "jsonValue must not be null");
        return keyword.getExpects().isEmpty() || keyword.getExpects().contains(jsonValue.getValueType());
    }
}
