package io.dugnutt.jsonschema.six;

import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonNumber;
import javax.validation.constraints.Min;
import java.util.Set;

/**
 * Number schema
 */
@Getter
@Builder(toBuilder = true, builderClassName = "NumberKeywordsBuilder")
@EqualsAndHashCode
public class NumberKeywords implements SchemaKeywords<JsonNumber> {

    private final boolean requiresNumber;
    private final boolean requiresInteger;
    private final Number minimum;
    private final Number maximum;
    @Min(1)
    private final Number multipleOf;
    private final Number exclusiveMinimum;
    private final Number exclusiveMaximum;

    @Override
    public Set<JsonSchemaType> getApplicableTypes() {
        return ImmutableSet.of(JsonSchemaType.NUMBER, JsonSchemaType.INTEGER);
    }

    // @Override
    public JsonSchemaGenerator toJson(JsonSchemaGenerator writer) {
        return writer.optionalWrite(JsonSchemaKeyword.MINIMUM, minimum)
                .optionalWrite(JsonSchemaKeyword.MAXIMUM, maximum)
                .optionalWrite(JsonSchemaKeyword.MULTIPLE_OF, multipleOf)
                .optionalWrite(JsonSchemaKeyword.EXCLUSIVE_MINIMUM, exclusiveMinimum)
                .optionalWrite(JsonSchemaKeyword.EXCLUSIVE_MAXIMUM, exclusiveMaximum);
    }

    static class NumberKeywordsBuilder {
    }
}
