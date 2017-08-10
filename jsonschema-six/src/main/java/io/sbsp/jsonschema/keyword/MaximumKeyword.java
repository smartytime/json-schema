package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class MaximumKeyword implements SchemaKeyword {

    private final Number maximum;
    private final boolean isExclusive;

    public MaximumKeyword(Number maximum, boolean isExclusive) {
        this.maximum = maximum;
        this.isExclusive = isExclusive;
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
        if (isExclusive) {
            generator.writeMax(SchemaKeyword.exclusiveMaximum, maximum);
        } else {
            generator.writeMax(SchemaKeyword.maximum, maximum);
        }
    }

    protected void writeDraft3And4(JsonSchemaGenerator generator) {
        generator.writeMax(SchemaKeyword.maximum, maximum);
        if (isExclusive) {
            generator.writeMax(SchemaKeyword.exclusiveMaximum, true);
        }
    }
}