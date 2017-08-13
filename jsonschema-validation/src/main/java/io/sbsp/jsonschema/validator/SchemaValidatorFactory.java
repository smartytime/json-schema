package io.sbsp.jsonschema.validator;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.FormatType;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.factory.KeywordValidatorCreator;
import io.sbsp.jsonschema.validator.factory.KeywordValidatorCreators;
import io.sbsp.jsonschema.validator.keywords.AdditionalPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.AllOfValidator;
import io.sbsp.jsonschema.validator.keywords.AnyOfValidator;
import io.sbsp.jsonschema.validator.keywords.ConstValidator;
import io.sbsp.jsonschema.validator.keywords.EnumValidator;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import io.sbsp.jsonschema.validator.keywords.NotKeywordValidator;
import io.sbsp.jsonschema.validator.keywords.OneOfValidator;
import io.sbsp.jsonschema.validator.keywords.TypeValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayContainsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayItemsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayMaxItemsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayMinItemsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayUniqueItemsValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberLimitValidators;
import io.sbsp.jsonschema.validator.keywords.number.NumberMultipleOfValidator;
import io.sbsp.jsonschema.validator.keywords.object.DependenciesValidator;
import io.sbsp.jsonschema.validator.keywords.object.MaxPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.MinPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.PatternPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.PropertyNameValidator;
import io.sbsp.jsonschema.validator.keywords.object.PropertySchemaValidator;
import io.sbsp.jsonschema.validator.keywords.object.RequiredPropertyValidator;
import io.sbsp.jsonschema.validator.keywords.string.StringFormatValidator;
import io.sbsp.jsonschema.validator.keywords.string.StringMaxLengthValidator;
import io.sbsp.jsonschema.validator.keywords.string.StringMinLengthValidator;
import io.sbsp.jsonschema.validator.keywords.string.StringPatternValidator;
import io.sbsp.jsonschema.validator.keywords.string.formatValidators.FormatValidator;
import lombok.NonNull;

import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class SchemaValidatorFactory {

    public static final SchemaValidatorFactory DEFAULT_VALIDATOR_FACTORY = new SchemaValidatorFactoryBuilder().build();

    private final Map<URI, SchemaValidator> validatorCache = new HashMap<>();

    @NonNull
    private final Map<String, FormatValidator> customFormatValidators;

    @NonNull
    private final KeywordValidatorCreators validators;

    @NonNull
    private final JsonProvider provider;

    public SchemaValidatorFactory(Map<String, FormatValidator> customFormatValidators,
                                  KeywordValidatorCreators factories,
                                  JsonProvider provider) {
        checkNotNull(customFormatValidators, "customFormatValidators must not be null");
        checkNotNull(factories, "factories must not be null");
        checkNotNull(provider, "provider must not be null");

        this.customFormatValidators = Collections.unmodifiableMap(customFormatValidators);
        this.validators = factories;
        this.provider = provider;
    }

    public static SchemaValidator createValidatorForSchema(Schema schema) {
        return DEFAULT_VALIDATOR_FACTORY.createValidator(schema);
    }

    void cacheValidator(URI schemaURI, SchemaValidator validator) {
        if (schemaURI.isAbsolute()) {
            validatorCache.putIfAbsent(schemaURI, validator);
        }
    }

    public List<SchemaValidator> createValidators(List<Schema> schemas) {
        return schemas.stream()
                .map(this::createValidator)
                .collect(Collectors.toList());
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

    public static SchemaValidatorFactoryBuilder builder() {
        return new SchemaValidatorFactoryBuilder();
    }

    public static class SchemaValidatorFactoryBuilder {
        private JsonProvider provider = JsonProvider.provider();
        private final Map<String, FormatValidator> customFormatValidators = new HashMap<>();
        private final SetMultimap<KeywordMetadata<?>, KeywordValidatorCreator<?, ?>> factories = HashMultimap.create();

        public SchemaValidatorFactoryBuilder() {
            withCommonValidators();
            initCoreFormatValidators();
        }

        public <K extends SchemaKeyword, V extends KeywordValidator<K>> SchemaValidatorFactoryBuilder addValidator(KeywordMetadata<K> keyword, KeywordValidatorCreator<K, V> factory) {
            factories.put(keyword, factory);
            return this;
        }

        public SchemaValidatorFactory build() {
            return new SchemaValidatorFactory(this.customFormatValidators, new KeywordValidatorCreators(this.factories), this.provider);
        }

        public SchemaValidatorFactoryBuilder addCustomFormatValidator(String format, FormatValidator formatValidator) {
            checkArgument(!Strings.isNullOrEmpty(format), "format must not be blank");
            checkNotNull(formatValidator, "formatValidator must not be null");
            this.customFormatValidators.put(format, formatValidator);
            return this;
        }

        public SchemaValidatorFactoryBuilder withCommonValidators() {

            // ########################################################### //
            // #########  COMMON VALIDATORS      ######################### //
            // ########################################################### //

            this.addValidator(Keywords.type, TypeValidator::new);
            this.addValidator(Keywords.$enum, EnumValidator::new);
            this.addValidator(Keywords.not, NotKeywordValidator::new);
            this.addValidator(Keywords.$const, ConstValidator::new);
            this.addValidator(Keywords.allOf, AllOfValidator::new);
            this.addValidator(Keywords.anyOf, AnyOfValidator::new);
            this.addValidator(Keywords.oneOf, OneOfValidator::new);

            // ########################################################### //
            // #########  STRING VALIDATORS      ######################### //
            // ########################################################### //
            this.addValidator(Keywords.maxLength, StringMaxLengthValidator::new);
            this.addValidator(Keywords.minLength, StringMinLengthValidator::new);
            this.addValidator(Keywords.pattern, StringPatternValidator::new);
            this.addValidator(Keywords.format, StringFormatValidator::new);

            // ########################################################### //
            // #########  ARRAY VALIDATORS      ######################### //
            // ########################################################### //

            this.addValidator(Keywords.maxItems, ArrayMaxItemsValidator::new);
            this.addValidator(Keywords.minItems, ArrayMinItemsValidator::new);
            this.addValidator(Keywords.uniqueItems, ArrayUniqueItemsValidator::new);
            this.addValidator(Keywords.items, ArrayItemsValidator::getArrayItemsValidator);
            this.addValidator(Keywords.contains, ArrayContainsValidator::new);

            // ########################################################### //
            // #########  NUMBER VALIDATORS      ######################### //
            // ########################################################### //
            this.addValidator(Keywords.minimum, NumberLimitValidators::getMinValidator);
            this.addValidator(Keywords.maximum, NumberLimitValidators::getMaxValidator);
            this.addValidator(Keywords.multipleOf, NumberMultipleOfValidator::new);

            // ########################################################### //
            // #########  OBJECT VALIDATORS      ######################### //
            // ########################################################### //
            this.addValidator(Keywords.properties, PropertySchemaValidator::new);
            this.addValidator(Keywords.propertyNames, PropertyNameValidator::new);
            this.addValidator(Keywords.required, RequiredPropertyValidator::new);
            this.addValidator(Keywords.patternProperties, PatternPropertiesValidator::new);
            this.addValidator(Keywords.additionalProperties, AdditionalPropertiesValidator::new);
            this.addValidator(Keywords.dependencies, DependenciesValidator::new);
            this.addValidator(Keywords.minProperties, MinPropertiesValidator::new);
            this.addValidator(Keywords.maxProperties, MaxPropertiesValidator::new);

            return this;
        }

        private void initCoreFormatValidators() {
            for (FormatType formatType : FormatType.values()) {
                customFormatValidators.put(formatType.toString(), FormatValidator.forFormat(formatType));
            }
        }
    }
}
