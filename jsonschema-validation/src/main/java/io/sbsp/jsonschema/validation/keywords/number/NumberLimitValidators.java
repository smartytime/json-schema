package io.sbsp.jsonschema.validation.keywords.number;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

public class NumberLimitValidators {

    public static KeywordValidator<LimitKeyword> getMaxValidator(LimitKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        if (keyword.isExclusive()) {
            return NumberExclusiveMaximumValidator.builder()
                    .schema(schema)
                    .exclusiveMaximum(keyword.getExclusiveLimit().doubleValue())
                    .build();
        } else {
            return NumberMaximumValidator.builder()
                    .schema(schema)
                    .maximum(keyword.getLimit().doubleValue())
                    .build();
        }
    }

    public static KeywordValidator<LimitKeyword> getMinValidator(LimitKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        if (keyword.isExclusive()) {
            return NumberExclusiveMinimumValidator.builder()
                    .schema(schema)
                    .exclusiveMinimum(keyword.getExclusiveLimit().doubleValue())
                    .build();
        } else {
            return NumberMinimumValidator.builder()
                    .schema(schema)
                    .minimum(keyword.getLimit().doubleValue())
                    .build();
        }
    }
}
