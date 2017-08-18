package io.sbsp.jsonschema.loading.keyword.versions.flex;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword.LimitKeywordBuilder;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.loading.SchemaLoader;
import io.sbsp.jsonschema.utils.JsonUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.TRUE;

/**
 * Class that handles extracting limit keywords (minimum,maximum,exclusiveMaximum,exclusiveMinimum) for all version
 * of the schema.  This class is complicated, but it saved a sort of class explosion for keywords/validators.
 */
@Getter
@EqualsAndHashCode
public class LimitKeywordDigester implements KeywordDigester<LimitKeyword> {

    private final KeywordInfo<LimitKeyword> keyword;
    private final KeywordInfo<LimitKeyword> exclusiveKeyword;
    private final Supplier<LimitKeyword> blankKeywordSupplier;
    private final List<KeywordInfo<LimitKeyword>> keywords;

    @Builder
    private LimitKeywordDigester(KeywordInfo<LimitKeyword> keyword, KeywordInfo<LimitKeyword> exclusiveKeyword,
                                 Supplier<LimitKeyword> blankKeywordSupplier) {
        this.keyword = checkNotNull(keyword);
        this.exclusiveKeyword = checkNotNull(exclusiveKeyword);
        this.blankKeywordSupplier = checkNotNull(blankKeywordSupplier);
        this.keywords = ImmutableList.of(keyword, exclusiveKeyword);
    }

    @Override
    public List<KeywordInfo<LimitKeyword>> getIncludedKeywords() {
        return keywords;
    }

    @Override
    public Optional<KeywordDigest<LimitKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {

        final JsonValueWithPath exclusiveValue = jsonObject.path(exclusiveKeyword.key());
        final JsonValueWithPath limitValue = jsonObject.path(keyword.key());

        final LimitKeywordBuilder keywordBuilder = LimitKeyword.builder(keyword, exclusiveKeyword);

        if (limitValue.isNotNull()) {
            keywordBuilder.limit(JsonUtils.toNumber(limitValue.asJsonNumber()));
        }

        if(exclusiveValue.isNotNull()) {
            keywordBuilder.exclusiveLimit(JsonUtils.toNumber(exclusiveValue.asJsonNumber()));
        }

        return KeywordDigest.ofOptional(keyword, keywordBuilder.build());
    }

    private static LimitKeywordDigesterBuilder builder() {
        return new LimitKeywordDigesterBuilder();
    }

    public static LimitKeywordDigester minimumExtractor() {
        return builder()
                .keyword(Keywords.MINIMUM)
                .exclusiveKeyword(Keywords.EXCLUSIVE_MINIMUM)
                .blankKeywordSupplier(LimitKeyword::minimumKeyword)
                .build();
    }

    public static LimitKeywordDigester maximumExtractor() {
        return builder()
                .keyword(Keywords.MAXIMUM)
                .exclusiveKeyword(Keywords.EXCLUSIVE_MAXIMUM)
                .blankKeywordSupplier(LimitKeyword::maximumKeyword)
                .build();
    }

    public static class LimitKeywordDigesterBuilder {
    }
}