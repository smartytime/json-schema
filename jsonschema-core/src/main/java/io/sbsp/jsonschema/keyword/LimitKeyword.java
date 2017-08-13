package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
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

    private final KeywordMetadata<LimitKeyword> keyword;
    private final KeywordMetadata<LimitKeyword> exclusiveKeyword;

    @Wither
    private final Number limit;

    @Wither
    private final Number exclusiveLimit;

    @Wither
    private final boolean isExclusive;

    protected LimitKeyword(KeywordMetadata<LimitKeyword> keyword, KeywordMetadata<LimitKeyword> exclusiveKeyword,
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
        return new LimitKeyword(Keywords.minimum, Keywords.exclusiveMinimum, null, null, false);
    }

    public static LimitKeyword maximumKeyword() {
        return new LimitKeyword(Keywords.maximum, Keywords.exclusiveMaximum, null, null, false);
    }

    @Override
    public void writeToGenerator(KeywordMetadata<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
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
        if (exclusiveLimit != null) {
            generator.write(exclusiveKeyword.getKey(), exclusiveLimit);
        }

        if (limit != null) {
            generator.write(keyword.getKey(), limit);
        }
    }

    protected void writeDraft3And4(JsonSchemaGenerator generator) {
        generator.write(keyword.getKey(), limit);
        if (isExclusive) {
            generator.write(exclusiveKeyword.getKey(), true);
        }
    }

    @Override
    public String toString() {
        final StringWriter stringWriter = new StringWriter();
        final JsonGenerator generator = provider().createGenerator(stringWriter);
        generator.writeStartObject();
        this.writeToGenerator(keyword, new JsonSchemaGenerator(generator), JsonSchemaVersion.Draft6);
        generator.writeEnd();
        generator.flush();
        return stringWriter.toString();
    }

    public static class LimitKeywordBuilder {
    }
}