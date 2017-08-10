package io.sbsp.jsonschema.extractor.keyword.versions;

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
public class Draft4MinimumKeywordExtractor implements SchemaKeywordExtractor {

    @Override
    public KeywordMetadata<?> getKeyword() {
        return Draft4SchemaKeyword.exclusiveMinimumDraft4;
    }

    @Override
    public ExtractionReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, ExtractionReport report) {
        boolean isExclusive = validateBoolean(Draft4SchemaKeyword.exclusiveMinimumDraft4, jsonObject, report).orElse(false);
        final Optional<JsonNumber> minimum = validateType(SchemaKeyword.minimum, jsonObject, report, JsonNumber.class);

        if (isExclusive && !minimum.isPresent()) {
            report.logMissingKeyword(Draft4SchemaKeyword.exclusiveMinimumDraft4, SchemaKeyword.minimum);
        }

        minimum.ifPresent(min -> {
            if (isExclusive) {
                builder.exclusiveMinimum(min.doubleValue());
            } else {
                builder.minimum(min.doubleValue());
            }
        });

        return report;
    }
}