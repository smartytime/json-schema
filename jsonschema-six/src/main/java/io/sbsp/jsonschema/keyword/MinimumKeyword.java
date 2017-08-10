package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class MinimumKeyword implements SchemaKeyword {

    private final Number minimum;
    private final boolean isExclusive;

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
            generator.writeMin(SchemaKeyword.exclusiveMinimum, minimum);
        } else {
            generator.writeMin(SchemaKeyword.minimum, minimum);
        }
    }

    protected void writeDraft3And4(JsonSchemaGenerator generator) {
        generator.writeMin(SchemaKeyword.minimum, minimum);
        if (isExclusive) {
            generator.writeMin(SchemaKeyword.exclusiveMinimum, true);
        }
    }
}