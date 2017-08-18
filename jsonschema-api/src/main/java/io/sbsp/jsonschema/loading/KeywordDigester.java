package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

/**
 * Responsible for extracting a keyword or keywords from a json document and loading them into a {@link SchemaBuilder}
 *
 * These loaders specify the keywords they process via {@link #getIncludedKeywords()}, which allows for flexible or
 * strict loading
 */
public interface KeywordDigester<K extends SchemaKeyword> {

    /**
     * This list of keywords allows the processing system to handle most of the validation around "flexible" schema
     * parsing - knowing when to raise certain schema incompatibilities as errors or warnings.
     * @return
     */
    List<KeywordInfo<K>> getIncludedKeywords();

    Optional<KeywordDigest<K>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report);

    // @SuppressWarnings("unchecked")
    // default <V extends JsonValue> Optional<V> validateType(KeywordInfo<?> keyword, JsonValueWithLocation from, LoadingReport report, Class<V> type) {
    //     checkNotNull(keyword, "keyword must not be null");
    //     checkNotNull(from, "from must not be null");
    //     final JsonValue jsonValue = from.getOrDefault(keyword.key(), JsonValue.NULL);
    //     if (keyword.getExpects().isEmpty()) {
    //         return Optional.of((V) jsonValue);
    //     } else if(keyword.getExpects().contains(jsonValue.getValueType())) {
    //         return Optional.of((V) jsonValue);
    //     } else {
    //         if (!JsonValue.NULL.equals(jsonValue)) {
    //             final SchemaLocation issueLocation = from.getLocation().child(keyword.key());
    //             report.error(typeMismatch(keyword, jsonValue, issueLocation));
    //         }
    //         return Optional.empty();
    //     }
    // }

    // default Optional<String> validateString(KeywordInfo<StringKeyword> keyword, JsonValueWithLocation from, LoadingReport report) {
    //     return validateType(keyword, from, report, JsonString.class).map(JsonString::getString);
    // }
    //
    // default boolean appliesTo(JsonSchemaVersion version) {
    //     return getKeyword().getVersionInfo(version).isPresent();
    // }
    //
    // default Optional<JsonObject> validateObject(KeywordInfo<SchemaMapKeyword> keyword, JsonValueWithLocation from, LoadingReport report) {
    //     return validateType(keyword, from, report, JsonObject.class);
    // }
    //
    // default Optional<JsonArray> validateArray(KeywordInfo<? extends SchemaListKeyword> keyword, JsonValueWithLocation from, LoadingReport report) {
    //     return validateType(keyword, from, report, JsonArray.class);
    // }
    //
    // default Optional<Boolean> validateBoolean(KeywordInfo<?> keyword, JsonValueWithLocation from, LoadingReport report) {
    //     return validateType(keyword, from, report, JsonValue.class).filter(v -> v == JsonValue.TRUE || v == JsonValue.FALSE).map(v-> v == JsonValue.TRUE);
    // }
    //
    // default Optional<Number> validateNumber(KeywordInfo<NumberKeyword> keyword, JsonValueWithLocation from, LoadingReport report) {
    //     return validateType(keyword, from, report, JsonNumber.class).map(JsonNumber::doubleValue);
    // }
}
