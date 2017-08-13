package io.sbsp.jsonschema.loading.keyword.versions.flex;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.keyword.SchemaKeywordExtractor;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.utils.JsonUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

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
public class LimitKeywordExtractor implements SchemaKeywordExtractor {

    private final KeywordMetadata<LimitKeyword> keyword;
    private final KeywordMetadata<LimitKeyword> exclusiveKeyword;
    private final Supplier<LimitKeyword> blankKeywordSupplier;
    private final boolean allowBooleanExclusive;
    private final boolean allowNumberExclusive;

    @Builder
    private LimitKeywordExtractor(KeywordMetadata<LimitKeyword> keyword, KeywordMetadata<LimitKeyword> exclusiveKeyword,
                                  Supplier<LimitKeyword> blankKeywordSupplier, boolean allowBooleanExclusive, boolean allowNumberExclusive) {
        this.keyword = checkNotNull(keyword);
        this.exclusiveKeyword = checkNotNull(exclusiveKeyword);
        this.blankKeywordSupplier = checkNotNull(blankKeywordSupplier);
        this.allowBooleanExclusive = allowBooleanExclusive;
        this.allowNumberExclusive = allowNumberExclusive;
    }

    @Override
    public KeywordMetadata<?> getKeyword() {
        return keyword;
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, LoadingReport report) {

        Number limit = null;
        Number exclusiveLimit = null;
        Boolean isExclusive = null;

        final JsonValueWithLocation exclusiveValue = jsonObject.getPathAwareObject(exclusiveKeyword.getKey());
        final JsonValueWithLocation limitValue = jsonObject.getPathAwareObject(keyword.getKey());

        if (limitValue.is(NUMBER)) {
            limit = JsonUtils.toNumber(limitValue.asJsonNumber());
        } else if (!limitValue.isNull()) {
            return report.error(typeMismatch(keyword, limitValue));
        }

        if (exclusiveValue.isBoolean() && allowBooleanExclusive) {
            isExclusive = exclusiveValue.is(TRUE);
        } else if (exclusiveValue.is(NUMBER) && allowNumberExclusive) {
            exclusiveLimit = JsonUtils.toNumber(exclusiveValue.asJsonNumber());
        } else if (exclusiveValue.isNotNull()) {
            return report.error(typeMismatch(exclusiveKeyword, exclusiveValue));
        }

        if (limit != null && exclusiveLimit == null && MoreObjects.firstNonNull(isExclusive, false)) {
            builder.numberExclusiveLimit(keyword, blankKeywordSupplier, limit);
        } else if (limit != null) {
            builder.numberLimit(keyword, blankKeywordSupplier, limit);
        }
        if (exclusiveLimit != null) {
            builder.numberExclusiveLimit(keyword, blankKeywordSupplier, exclusiveLimit);
        }

        return report;
    }

    private static LimitKeywordExtractorBuilder builder() {
        return new LimitKeywordExtractorBuilder();
    }

    public static LimitKeywordExtractor flexibleMinimumExtractor() {
        return builder()
                .keyword(Keywords.minimum)
                .exclusiveKeyword(Keywords.exclusiveMinimum)
                .blankKeywordSupplier(LimitKeyword::minimumKeyword)
                .allowBooleanExclusive(true)
                .allowNumberExclusive(true)
                .build();
    }

    public static LimitKeywordExtractor flexibleMaximumExtractor() {
        return builder()
                .keyword(Keywords.maximum)
                .exclusiveKeyword(Keywords.exclusiveMaximum)
                .blankKeywordSupplier(LimitKeyword::maximumKeyword)
                .allowBooleanExclusive(true)
                .allowNumberExclusive(true)
                .build();
    }

    public static LimitKeywordExtractor draft4MinimumExtractor() {
        return builder()
                .keyword(Keywords.minimum)
                .exclusiveKeyword(Keywords.exclusiveMinimum)
                .blankKeywordSupplier(LimitKeyword::minimumKeyword)
                .allowBooleanExclusive(true)
                .build();
    }

    public static LimitKeywordExtractor draft4MaximumExtractor() {
        return builder()
                .keyword(Keywords.maximum)
                .exclusiveKeyword(Keywords.exclusiveMaximum)
                .blankKeywordSupplier(LimitKeyword::maximumKeyword)
                .allowBooleanExclusive(true)
                .build();
    }

    public static LimitKeywordExtractor draft6MinimumExtractor() {
        return builder()
                .keyword(Keywords.minimum)
                .exclusiveKeyword(Keywords.exclusiveMinimum)
                .blankKeywordSupplier(LimitKeyword::minimumKeyword)
                .allowNumberExclusive(true)
                .build();
    }

    public static LimitKeywordExtractor draft6MaximumExtractor() {
        return builder()
                .keyword(Keywords.maximum)
                .exclusiveKeyword(Keywords.exclusiveMaximum)
                .blankKeywordSupplier(LimitKeyword::maximumKeyword)
                .allowNumberExclusive(true)
                .build();
    }

    public static LimitKeywordExtractor minimumExtractor(JsonSchemaVersion version) {
        return version == JsonSchemaVersion.Draft6 ? draft6MinimumExtractor() : draft4MinimumExtractor();
    }

    public static LimitKeywordExtractor maximumExtractor(JsonSchemaVersion version) {
        return version == JsonSchemaVersion.Draft6 ? draft6MaximumExtractor() : draft4MaximumExtractor();
    }

    public static class LimitKeywordExtractorBuilder {
    }
}