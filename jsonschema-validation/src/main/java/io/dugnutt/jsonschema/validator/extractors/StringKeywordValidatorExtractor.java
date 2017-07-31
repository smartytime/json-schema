package io.dugnutt.jsonschema.validator.extractors;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.keywords.StringKeywords;
import io.dugnutt.jsonschema.validator.extractors.KeywordValidators.KeywordValidatorsBuilder;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;
import io.dugnutt.jsonschema.validator.keywords.string.StringFormatValidator;
import io.dugnutt.jsonschema.validator.keywords.string.StringMaxLengthValidator;
import io.dugnutt.jsonschema.validator.keywords.string.StringMinLengthValidator;
import io.dugnutt.jsonschema.validator.keywords.string.StringPatternValidator;

import javax.json.JsonValue.ValueType;
import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringKeywordValidatorExtractor implements KeywordValidatorExtractor {

    public static StringKeywordValidatorExtractor stringKeywordsValidator() {
        return new StringKeywordValidatorExtractor();
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        checkNotNull(schema, "schema must not be null");
        return schema.hasStringKeywords();
    }

    @Override
    public KeywordValidators getKeywordValidators(Schema schema, SchemaValidatorFactory factory) {
        KeywordValidatorsBuilder validators = KeywordValidators.builder()
                .schema(schema)
                .validatorFactory(factory);
        if (schema.hasStringKeywords()) {
            final StringKeywords keywords = schema.getStringKeywords();



            // ########################################
            // MAX_LENGTH
            // ########################################

            if (keywords.getMaxLength() != null) {
                validators.addValidator(StringMaxLengthValidator.builder()
                        .schema(schema)
                        .maxLength(keywords.getMaxLength())
                        .build());
            }

            // ########################################
            // MIN_LENGTH
            // ########################################

            if (keywords.getMinLength() != null) {
                validators.addValidator(StringMinLengthValidator.builder()
                        .schema(schema)
                        .minLength(keywords.getMinLength())
                        .build());
            }

            // ########################################
            // PATTERN
            // ########################################

            keywords.findPattern().ifPresent(pattern -> {
                validators.addValidator(StringPatternValidator.builder()
                        .schema(schema)
                        .pattern(keywords.getPattern())
                        .build());
            });

            // ########################################
            // FORMAT
            // ########################################

            factory.getFormatValidator(keywords.getFormat()).ifPresent(formatValidator -> {
                validators.addValidator(StringFormatValidator.builder()
                        .schema(schema)
                        .formatValidator(formatValidator)
                        .build());
            });
        }
        return validators.build();
    }

    @Override
    public Set<ValueType> getApplicableTypes() {
        return Collections.singleton(ValueType.STRING);
    }
}
