package io.sbsp.jsonschema.extractor.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.extractor.ExtractionReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import javax.json.JsonString;
import javax.json.JsonValue;

public class TypeKeywordExtractor implements SchemaKeywordExtractor {

    @Override
    public KeywordMetadata<?> getKeyword() {
        return SchemaKeyword.type;
    }

    @Override
    public ExtractionReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory factory, ExtractionReport report) {
        validateType(SchemaKeyword.type, jsonObject, report, JsonValue.class).ifPresent(typeValue -> {
            if (typeValue.getValueType() == JsonValue.ValueType.ARRAY) {
                typeValue.asJsonArray().forEach(value -> {
                    builder.type(JsonSchemaType.fromString(((JsonString) value).getString()));
                });
            } else {
                final String typeString = ((JsonString) typeValue).getString();
                builder.type(JsonSchemaType.fromString(typeString));
            }
        });
        return report;
    }
}
