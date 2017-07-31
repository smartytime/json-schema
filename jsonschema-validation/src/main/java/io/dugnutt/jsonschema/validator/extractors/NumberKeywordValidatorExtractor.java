package io.dugnutt.jsonschema.validator.extractors;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;
import io.dugnutt.jsonschema.validator.extractors.KeywordValidators.KeywordValidatorsBuilder;
import io.dugnutt.jsonschema.validator.keywords.number.NumberExclusiveMaximumValidator;
import io.dugnutt.jsonschema.validator.keywords.number.NumberExclusiveMinimumValidator;
import io.dugnutt.jsonschema.validator.keywords.number.NumberMaximumValidator;
import io.dugnutt.jsonschema.validator.keywords.number.NumberMinimumValidator;
import io.dugnutt.jsonschema.validator.keywords.number.NumberMultipleOfValidator;

import java.util.Collections;
import java.util.Set;

import static javax.json.JsonValue.ValueType;

public class NumberKeywordValidatorExtractor implements KeywordValidatorExtractor {

    public static NumberKeywordValidatorExtractor numberKeywordsValidator() {
        return new NumberKeywordValidatorExtractor();
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        return schema.getNumberKeywords().isPresent();
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
        schema.getNumberKeywords().ifPresent(keywords->{

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
        });

        return validators.build();
    }
}
