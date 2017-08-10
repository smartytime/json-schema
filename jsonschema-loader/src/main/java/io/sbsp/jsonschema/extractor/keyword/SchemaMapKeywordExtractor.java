package io.sbsp.jsonschema.extractor.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.extractor.ExtractionReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class SchemaMapKeywordExtractor implements SchemaKeywordExtractor {

    @Getter
    private final KeywordMetadata<SchemaMapKeyword> keyword;

    public SchemaMapKeywordExtractor(KeywordMetadata<SchemaMapKeyword> keyword) {
        this.keyword = checkNotNull(keyword, "keyword must not be null");
    }

    @Override
    public ExtractionReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, ExtractionReport report) {
        Map<String, JsonSchemaBuilder> keyedSchemas = new LinkedHashMap<>();
        validateObject(keyword, jsonObject, report).ifPresent(propObject -> {
            propObject.forEach((key, value) -> {
                if (value.getValueType() != JsonValue.ValueType.OBJECT) {
                    report.logTypeMismatch(keyword, value);
                    return;
                }
                final SchemaLocation childLocation = jsonObject.getLocation().child(keyword.getKey(), key);
                final JsonValueWithLocation childValue = JsonValueWithLocation.fromJsonValue(value, childLocation);
                keyedSchemas.put(key, schemaFactory.createSchemaBuilder(childValue, report));
            });
            builder.putAllKeywordSchemas(keyword, keyedSchemas);
        });
        return report;
    }
}
