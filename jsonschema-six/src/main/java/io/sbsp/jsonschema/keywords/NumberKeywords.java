package io.sbsp.jsonschema.keywords;

import com.google.common.base.Objects;
import io.sbsp.jsonschema.JsonSchemaGenerator;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.enums.JsonSchemaType;
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
@EqualsAndHashCode(doNotUseGetters = true)
public class NumberKeywords implements SchemaKeywords {

    private static final NumberKeywords BLANK_NUMBER_KEYWORDS = builder().build();
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
        return writer.optionalWrite(JsonSchemaKeywordType.MINIMUM, minimum)
                .optionalWrite(JsonSchemaKeywordType.MAXIMUM, maximum)
                .optionalWrite(JsonSchemaKeywordType.MULTIPLE_OF, multipleOf)
                .optionalWrite(JsonSchemaKeywordType.EXCLUSIVE_MINIMUM, exclusiveMinimum)
                .optionalWrite(JsonSchemaKeywordType.EXCLUSIVE_MAXIMUM, exclusiveMaximum);
    }

    public static NumberKeywords blankNumberKeywords() {
        return BLANK_NUMBER_KEYWORDS;
    }

    public static class NumberKeywordsBuilder {
        @Override
        public final int hashCode() {
            return Objects.hashCode(minimum, maximum, multipleOf, exclusiveMinimum, exclusiveMaximum);
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NumberKeywordsBuilder)) {
                return false;
            }
            final NumberKeywordsBuilder that = (NumberKeywordsBuilder) o;
            return Objects.equal(minimum, that.minimum) &&
                    Objects.equal(maximum, that.maximum) &&
                    Objects.equal(multipleOf, that.multipleOf) &&
                    Objects.equal(exclusiveMinimum, that.exclusiveMinimum) &&
                    Objects.equal(exclusiveMaximum, that.exclusiveMaximum);
        }
    }
}
