package io.sbsp.jsonschema.extractor.keyword.versions.draft4;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.extractor.ExtractionReport;
import io.sbsp.jsonschema.extractor.keyword.SchemaKeywordExtractor;
import io.sbsp.jsonschema.keyword.Draft4SchemaKeyword;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonNumber;
import java.util.Optional;

@Getter
@EqualsAndHashCode
public class Draft4AdditionalItemsKeywordExtractor implements SchemaKeywordExtractor {

    @Override
    public KeywordMetadata<?> getKeyword() {
        return Draft4SchemaKeyword.exclusiveMaximumDraft4;
    }

    @Override
    public ExtractionReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, ExtractionReport report) {

        boolean isExclusive = validateBoolean(Draft4SchemaKeyword.exclusiveMaximumDraft4, jsonObject, report).orElse(false);
        final Optional<JsonNumber> maximum = validateType(SchemaKeyword.maximum, jsonObject, report, JsonNumber.class);

        if (isExclusive && !maximum.isPresent()) {
            report.logMissingKeyword(Draft4SchemaKeyword.exclusiveMaximumDraft4, SchemaKeyword.maximum);
        }

        maximum.ifPresent(max->{
            if (isExclusive) {
                builder.exclusiveMaximum(max.doubleValue());
            } else {
                builder.maximum(max.doubleValue());
            }
        });

        return report;
    }
}