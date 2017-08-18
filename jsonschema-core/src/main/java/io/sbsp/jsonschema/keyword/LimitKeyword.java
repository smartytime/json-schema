package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

import javax.json.stream.JsonGenerator;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.json.spi.JsonProvider.provider;

@Getter
@EqualsAndHashCode
public class LimitKeyword implements SchemaKeyword {

    private final KeywordInfo<LimitKeyword> keyword;
    private final KeywordInfo<LimitKeyword> exclusiveKeyword;

    @Wither
    private final Number limit;

    @Wither
    private final Number exclusiveLimit;

    @Wither
    private final boolean isExclusive;

    @Builder
    protected LimitKeyword(KeywordInfo<LimitKeyword> keyword, KeywordInfo<LimitKeyword> exclusiveKeyword,
                           Number limit,
                           Number exclusiveLimit,
                           boolean isExclusive) {
        this.keyword = checkNotNull(keyword);
        this.exclusiveKeyword = checkNotNull(exclusiveKeyword);
        this.limit = limit;
        this.isExclusive = isExclusive || exclusiveLimit != null;
        if (isExclusive && exclusiveLimit == null && limit != null) {
            this.exclusiveLimit = limit;
        } else {
            this.exclusiveLimit = exclusiveLimit;
        }
    }

    public static LimitKeyword minimumKeyword() {
        return new LimitKeyword(Keywords.MINIMUM, Keywords.EXCLUSIVE_MINIMUM, null, null, false);
    }

    public static LimitKeyword maximumKeyword() {
        return new LimitKeyword(Keywords.MAXIMUM, Keywords.EXCLUSIVE_MAXIMUM, null, null, false);
    }

    public static LimitKeywordBuilder builder(final KeywordInfo<LimitKeyword> keyword, final KeywordInfo<LimitKeyword> exclusiveKeyword) {
        return new LimitKeywordBuilder().keyword(keyword).exclusiveKeyword(exclusiveKeyword);
    }

    @Override
    public void writeJson(KeywordInfo<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        switch (version) {
            case Draft6:
                writeDraft6(generator);
                return;
            case Custom:
            case Unknown:
                throw new IllegalArgumentException("Unknown output type: Custom");
            default:
                writeDraft3And4(generator);
        }
    }

    protected void writeDraft6(JsonSchemaGenerator generator) {
        if (limit != null) {
            generator.write(keyword.key(), limit);
        }

        if (exclusiveLimit != null) {
            generator.write(exclusiveKeyword.key(), exclusiveLimit);
        }
    }

    protected void writeDraft3And4(JsonSchemaGenerator generator) {
        if (limit != null && exclusiveLimit != null) {
            throw new IllegalStateException(String.format("Versions to loading do not support number values for %s and %s",
                    keyword.key(), exclusiveKeyword.key()));
        }
        if (isExclusive) {
            generator.write(keyword.key(), exclusiveLimit);
            generator.write(exclusiveKeyword.key(), true);
        } else {
            generator.write(keyword.key(), limit);
        }
    }

    @Override
    public String toString() {
        final StringWriter stringWriter = new StringWriter();
        final JsonGenerator generator = provider().createGenerator(stringWriter);
        generator.writeStartObject();
        this.writeJson(keyword, new JsonSchemaGenerator(generator), JsonSchemaVersion.Draft6);
        generator.writeEnd();
        generator.flush();
        return stringWriter.toString();
    }

    public static class LimitKeywordBuilder {
    }
}