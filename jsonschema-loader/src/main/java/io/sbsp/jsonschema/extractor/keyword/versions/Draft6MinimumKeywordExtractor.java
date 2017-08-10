package io.sbsp.jsonschema.extractor.keyword.versions;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.extractor.ExtractionReport;
import io.sbsp.jsonschema.extractor.keyword.SchemaKeywordExtractor;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonNumber;
import java.util.Optional;

@Getter
@EqualsAndHashCode
public class Draft6MinimumKeywordExtractor implements SchemaKeywordExtractor {

    @Override
    public KeywordMetadata<?> getKeyword() {
        return SchemaKeyword.exclusiveMaximum;
    }

    @Override
    public ExtractionReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory extractor, ExtractionReport report) {
        final Optional<JsonNumber> exclusiveMin = validateType(SchemaKeyword.exclusiveMinimum, jsonObject, report, JsonNumber.class);
        if (!exclusiveMin.isPresent() && jsonObject.containsKey(SchemaKeyword.exclusiveMinimum.getKey())) {
            //In this case, we can't apply any other keywords, like minimum because we've encountered an older version of the
            //exclusiveMinimum keyword
            return report;
        }
        exclusiveMin.map(JsonNumber::doubleValue)
                .ifPresent(builder::exclusiveMinimum);

        validateType(SchemaKeyword.minimum, jsonObject, report, JsonNumber.class)
                .map(JsonNumber::doubleValue)
                .ifPresent(builder::minimum);

        return report;
    }
}