package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import javax.json.JsonString;
import javax.json.JsonValue;

public class TypeKeywordExtractor implements SchemaKeywordExtractor {

    @Override
    public KeywordMetadata<?> getKeyword() {
        return Keywords.type;
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory factory, LoadingReport report) {
        validateType(Keywords.type, jsonObject, report, JsonValue.class).ifPresent(typeValue -> {
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
