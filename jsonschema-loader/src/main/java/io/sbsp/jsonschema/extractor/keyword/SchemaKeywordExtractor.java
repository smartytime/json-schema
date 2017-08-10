package io.sbsp.jsonschema.extractor.keyword;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.extractor.ExtractionReport;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import io.sbsp.jsonschema.keyword.StringKeyword;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public interface SchemaKeywordExtractor {
    ExtractionReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, ExtractionReport report);

    KeywordMetadata<?> getKeyword();

    @SuppressWarnings("unchecked")
    default <V extends JsonValue> Optional<V> validateType(KeywordMetadata<?> keyword, JsonValueWithLocation from, ExtractionReport report, Class<V> type) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(from, "from must not be null");
        final JsonValue jsonValue = from.getOrDefault(keyword.getKey(), JsonValue.NULL);
        if (keyword.getExpects().isEmpty()) {
            return Optional.of((V) jsonValue);
        } else if(keyword.getExpects().contains(jsonValue.getValueType())) {
            return Optional.of((V) jsonValue);
        } else {
            if (!JsonValue.NULL.equals(jsonValue)) {
                report.logTypeMismatch(keyword, jsonValue);
            }
            return Optional.empty();
        }
    }

    default Optional<String> validateString(KeywordMetadata<StringKeyword> keyword, JsonValueWithLocation from, ExtractionReport report) {
        return validateType(keyword, from, report, JsonString.class).map(JsonString::getString);
    }

    default Optional<JsonObject> validateObject(KeywordMetadata<SchemaMapKeyword> keyword, JsonValueWithLocation from, ExtractionReport report) {
        return validateType(keyword, from, report, JsonObject.class);
    }

    default Optional<JsonArray> validateArray(KeywordMetadata<? extends SchemaListKeyword> keyword, JsonValueWithLocation from, ExtractionReport report) {
        return validateType(keyword, from, report, JsonArray.class);
    }

    default Optional<Boolean> validateBoolean(KeywordMetadata<?> keyword, JsonValueWithLocation from, ExtractionReport report) {
        return validateType(keyword, from, report, JsonValue.class).filter(v -> v == JsonValue.TRUE || v == JsonValue.FALSE).map(v-> v == JsonValue.TRUE);
    }

    default Optional<Number> validateNumber(KeywordMetadata<NumberKeyword> keyword, JsonValueWithLocation from, ExtractionReport report) {
        return validateType(keyword, from, report, JsonNumber.class).map(JsonNumber::doubleValue);
    }
}
