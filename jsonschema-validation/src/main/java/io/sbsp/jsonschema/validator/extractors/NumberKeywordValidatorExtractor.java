package io.sbsp.jsonschema.validator.extractors;

import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.keywords.NumberKeywords;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.extractors.KeywordValidators.KeywordValidatorsBuilder;
import io.sbsp.jsonschema.validator.keywords.number.NumberExclusiveMaximumValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberExclusiveMinimumValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberMaximumValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberMinimumValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberMultipleOfValidator;

import java.util.Collections;
import java.util.Set;

import static javax.json.JsonValue.ValueType;

public class NumberKeywordValidatorExtractor implements KeywordValidatorExtractor {

    public static NumberKeywordValidatorExtractor numberKeywordsValidator() {
        return new NumberKeywordValidatorExtractor();
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        return schema.hasNumberKeywords();
    }

    @Override
    public Set<ValueType> getApplicableTypes() {
        return Collections.singleton(ValueType.NUMBER);
    }

    @Override
    public KeywordValidators getKeywordValidators(Schema schema, SchemaValidatorFactory factory) {
        final KeywordValidatorsBuilder validators = KeywordValidators.builder()
                .schema(schema)
                .validatorFactory(factory);
        if (schema.hasNumberKeywords()) {
            final NumberKeywords keywords = schema.getNumberKeywords();

            if (keywords.getMinimum() != null) {
                validators.addValidator(NumberMinimumValidator.builder()
                        .schema(schema)
                        .minimum(keywords.getMinimum().doubleValue())
                        .build());
            }

            if (keywords.getMaximum() != null) {
                validators.addValidator(NumberMaximumValidator.builder()
                        .schema(schema)
                        .maximum(keywords.getMaximum().doubleValue())
                        .build());
            }

            if (keywords.getExclusiveMaximum() != null) {
                validators.addValidator(NumberExclusiveMaximumValidator.builder()
                        .schema(schema)
                        .exclusiveMaximum(keywords.getExclusiveMaximum().doubleValue())
                        .build());
            }
            
            if (keywords.getExclusiveMinimum() != null) {
                validators.addValidator(NumberExclusiveMinimumValidator.builder()
                        .schema(schema)
                        .exclusiveMinimum(keywords.getExclusiveMinimum().doubleValue())
                        .build());
            }
            if (keywords.getMultipleOf() != null) {
                validators.addValidator(NumberMultipleOfValidator.builder()
                        .schema(schema)
                        .multipleOf(keywords.getMultipleOf())
                        .build());
            }
        }

        return validators.build();
    }
}
