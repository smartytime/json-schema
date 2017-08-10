package io.sbsp.jsonschema.extractor.impl;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.extractor.ExtractionReport;
import io.sbsp.jsonschema.extractor.SchemaExtractor;
import io.sbsp.jsonschema.extractor.keyword.SchemaKeywordExtractor;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchemaExtractorImpl implements SchemaExtractor {

    protected final List<SchemaKeywordExtractor> keywordExtractors;

    public SchemaExtractorImpl(List<SchemaKeywordExtractor> keywordExtractors) {
        checkNotNull(keywordExtractors, "keywordExtractors'' must not be null");
        this.keywordExtractors = ImmutableList.copyOf(keywordExtractors);
    }

    @Override
    public JsonSchemaBuilder extractSchema(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory factory, ExtractionReport report) {
        for (SchemaKeywordExtractor keywordExtractor : keywordExtractors) {
            keywordExtractor.extractKeyword(jsonObject, builder, factory, report);
        }
        return builder;
    }
}
