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
public class Draft6MaximumKeywordExtractor implements SchemaKeywordExtractor {

    @Override
    public KeywordMetadata<?> getKeyword() {
        return SchemaKeyword.exclusiveMaximum;
    }

    @Override
    public ExtractionReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, ExtractionReport report) {
        final Optional<JsonNumber> exclusiveMax = validateType(SchemaKeyword.exclusiveMaximum, jsonObject, report, JsonNumber.class);
        if (!exclusiveMax.isPresent() && jsonObject.containsKey(SchemaKeyword.exclusiveMaximum.getKey())) {
            //In this case, we can't apply any other keywords, like maximum because we've encountered an older version of the
            //exclusiveMaximum keyword
            return report;
        }
        exclusiveMax.map(JsonNumber::doubleValue)
                .ifPresent(builder::exclusiveMaximum);

        validateType(SchemaKeyword.maximum, jsonObject, report, JsonNumber.class)
                .map(JsonNumber::doubleValue)
                .ifPresent(builder::maximum);

        return report;
    }
}