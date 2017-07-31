package io.sbsp.jsonschema.validator;

import com.google.common.base.Strings;
import io.sbsp.jsonschema.six.enums.FormatType;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.extractors.BaseKeywordValidatorExtractor;
import io.sbsp.jsonschema.validator.extractors.KeywordValidatorExtractor;
import io.sbsp.jsonschema.validator.keywords.string.formatValidators.FormatValidator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.validator.extractors.ArrayKeywordValidatorExtractor.arrayKeywordsValidator;
import static io.sbsp.jsonschema.validator.extractors.NumberKeywordValidatorExtractor.numberKeywordsValidator;
import static io.sbsp.jsonschema.validator.extractors.ObjectKeywordValidatorExtractor.objectKeywordsValidator;
import static io.sbsp.jsonschema.validator.extractors.StringKeywordValidatorExtractor.stringKeywordsValidator;

@Builder(builderClassName = "Builder", toBuilder = true)
public class SchemaValidatorFactory {

    public static final SchemaValidatorFactory DEFAULT_VALIDATOR_FACTORY = builder().build();

    private final Map<URI, SchemaValidator> validatorCache = new HashMap<>();

    @NonNull
    private final Map<String, FormatValidator> customFormatValidators;

    @NonNull
    @Singular
    private final List<KeywordValidatorExtractor> validators;

    private final JsonProvider provider;

    public static SchemaValidator createValidatorForSchema(Schema schema) {
        return DEFAULT_VALIDATOR_FACTORY.createValidator(schema);
    }

    void cacheValidator(URI schemaURI, SchemaValidator validator) {
        if (schemaURI.isAbsolute()) {
            validatorCache.putIfAbsent(schemaURI, validator);
        }
    }

    public SchemaValidator createValidator(Schema schema) {
        checkNotNull(schema, "schema must not be null when creating validator");
        final URI schemaURI = schema.getLocation().getUniqueURI();
        final SchemaValidator cachedValue = validatorCache.get(schemaURI);
        if (cachedValue != null) {
            return cachedValue;
        } else {
            final SchemaValidator validator;
            validator = JsonSchemaValidator.jsonSchemaValidator()
                    .validatorFactory(this)
                    .schema(schema)
                    .factories(validators)
                    .build();
            this.cacheValidator(schemaURI, validator);
            return validator;
        }
    }

    public Optional<FormatValidator> getFormatValidator(String input) {
        if (input == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(customFormatValidators.get(input));
    }

    public JsonProvider getProvider() {
        return provider;
    }

    public static class Builder {
        public Builder() {
            this.provider = JsonProvider.provider();
            this.customFormatValidators = new HashMap<>();
            this.validators = new ArrayList<>();
            initCoreSchemaValidators();
            initCoreFormatValidators();
        }

        public Builder customFormatValidator(String format, FormatValidator formatValidator) {
            checkArgument(!Strings.isNullOrEmpty(format), "format must not be blank");
            checkNotNull(formatValidator, "formatValidator must not be null");
            customFormatValidators.put(format, formatValidator);
            return this;
        }

        private void initCoreSchemaValidators() {
            // ##########################################
            // BASE VALIDATOR
            // ##########################################
            validator(BaseKeywordValidatorExtractor.baseSchemaValidator());

            // ##########################################
            // KEYWORD VALIDATORS
            // ##########################################
            validator(stringKeywordsValidator());
            validator(numberKeywordsValidator());
            validator(arrayKeywordsValidator());
            validator(objectKeywordsValidator());
        }

        private void initCoreFormatValidators() {
            for (FormatType formatType : FormatType.values()) {
                customFormatValidators.put(formatType.toString(), FormatValidator.forFormat(formatType));
            }
        }
    }
}