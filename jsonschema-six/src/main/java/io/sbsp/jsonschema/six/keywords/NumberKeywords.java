package io.sbsp.jsonschema.six.keywords;

import io.sbsp.jsonschema.six.JsonSchemaGenerator;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.Min;
import java.util.EnumSet;
import java.util.Set;

/**
 * Number schema
 */
@Getter
@Builder(toBuilder = true, builderClassName = "NumberKeywordsBuilder")
@EqualsAndHashCode
public class NumberKeywords implements SchemaKeywords {

    private final Number minimum;
    private final Number maximum;
    @Min(1)
    private final Number multipleOf;
    private final Number exclusiveMinimum;
    private final Number exclusiveMaximum;

    @Override
    public Set<JsonSchemaType> getApplicableTypes() {
        return EnumSet.of(JsonSchemaType.NUMBER, JsonSchemaType.INTEGER);
    }

    // @Override
    public JsonSchemaGenerator toJson(JsonSchemaGenerator writer) {
        return writer.optionalWrite(JsonSchemaKeyword.MINIMUM, minimum)
                .optionalWrite(JsonSchemaKeyword.MAXIMUM, maximum)
                .optionalWrite(JsonSchemaKeyword.MULTIPLE_OF, multipleOf)
                .optionalWrite(JsonSchemaKeyword.EXCLUSIVE_MINIMUM, exclusiveMinimum)
                .optionalWrite(JsonSchemaKeyword.EXCLUSIVE_MAXIMUM, exclusiveMaximum);
    }

    public static class NumberKeywordsBuilder {
    }

    private static final NumberKeywords BLANK_NUMBER_KEYWORDS = builder().build();

    public static NumberKeywords blankNumberKeywords() {
        return BLANK_NUMBER_KEYWORDS;
    }
}