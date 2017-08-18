package io.sbsp.jsonschema.validation;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.FormatType;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.utils.Schemas;
import io.sbsp.jsonschema.validation.factory.KeywordValidatorCreator;
import io.sbsp.jsonschema.validation.factory.KeywordValidatorCreators;
import io.sbsp.jsonschema.validation.keywords.AdditionalPropertiesValidator;
import io.sbsp.jsonschema.validation.keywords.AllOfValidator;
import io.sbsp.jsonschema.validation.keywords.AnyOfValidator;
import io.sbsp.jsonschema.validation.keywords.ConstValidator;
import io.sbsp.jsonschema.validation.keywords.EnumValidator;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import io.sbsp.jsonschema.validation.keywords.NotKeywordValidator;
import io.sbsp.jsonschema.validation.keywords.OneOfValidator;
import io.sbsp.jsonschema.validation.keywords.TypeValidator;
import io.sbsp.jsonschema.validation.keywords.array.ArrayContainsValidator;
import io.sbsp.jsonschema.validation.keywords.array.ArrayItemsValidator;
import io.sbsp.jsonschema.validation.keywords.array.ArrayMaxItemsValidator;
import io.sbsp.jsonschema.validation.keywords.array.ArrayMinItemsValidator;
import io.sbsp.jsonschema.validation.keywords.array.ArrayUniqueItemsValidator;
import io.sbsp.jsonschema.validation.keywords.number.NumberLimitValidators;
import io.sbsp.jsonschema.validation.keywords.number.NumberMultipleOfValidator;
import io.sbsp.jsonschema.validation.keywords.object.DependenciesValidator;
import io.sbsp.jsonschema.validation.keywords.object.MaxPropertiesValidator;
import io.sbsp.jsonschema.validation.keywords.object.MinPropertiesValidator;
import io.sbsp.jsonschema.validation.keywords.object.PatternPropertiesValidator;
import io.sbsp.jsonschema.validation.keywords.object.PropertyNameValidator;
import io.sbsp.jsonschema.validation.keywords.object.PropertySchemaValidator;
import io.sbsp.jsonschema.validation.keywords.object.RequiredPropertyValidator;
import io.sbsp.jsonschema.validation.keywords.string.StringFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.StringMaxLengthValidator;
import io.sbsp.jsonschema.validation.keywords.string.StringMinLengthValidator;
import io.sbsp.jsonschema.validation.keywords.string.StringPatternValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.ColorFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.DateFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.DateTimeFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.EmailFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.HostnameFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.IPV4Validator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.IPV6Validator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.JsonPointerValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.NoopFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.PatternBasedValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.PhoneFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.RegexFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.TimeFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.URIFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.URIReferenceFormatValidator;
import io.sbsp.jsonschema.validation.keywords.string.formatValidators.URITemplateFormatValidator;
import lombok.NonNull;

import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

public class SchemaValidatorFactoryImpl implements SchemaValidatorFactory {

    public static final SchemaValidatorFactory DEFAULT_VALIDATOR_FACTORY = new SchemaValidatorFactoryBuilder().build();

    private final Map<URI, SchemaValidator> validatorCache = new HashMap<>();

    @NonNull
    private final Map<String, FormatValidator> customFormatValidators;

    @NonNull
    private final KeywordValidatorCreators validators;

    @NonNull
    private final JsonProvider provider;

    public SchemaValidatorFactoryImpl(Map<String, FormatValidator> customFormatValidators,
                                      KeywordValidatorCreators factories,
                                      JsonProvider provider) {
        checkNotNull(customFormatValidators, "customFormatValidators must not be null");
        checkNotNull(factories, "factories must not be null");
        checkNotNull(provider, "provider must not be null");

        this.customFormatValidators = Collections.unmodifiableMap(customFormatValidators);
        this.validators = factories;
        this.provider = provider;
    }

    public SchemaValidatorFactoryImpl() {
        final SchemaValidatorFactoryImpl blankValidator = builder().build();
        this.customFormatValidators = blankValidator.customFormatValidators;
        this.validators = blankValidator.validators;
        this.provider = blankValidator.provider;
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

    @Override
    public SchemaValidator createValidator(Schema schema) {
        checkNotNull(schema, "schema must not be null when creating validation");

        if (Schemas.nullSchema().equals(schema)) {
            return NullSchemaValidator.getInstance();
        } else if (Schemas.falseSchema().equals(schema)) {
            return FalseSchemaValidator.getInstance();
        }


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

    @Override
    public Optional<FormatValidator> getFormatValidator(String input) {
        if (input == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(customFormatValidators.get(input));
    }

    public JsonProvider getJsonProvider() {
        return provider;
    }

    public static SchemaValidatorFactoryBuilder builder() {
        return new SchemaValidatorFactoryBuilder();
    }

    public static class SchemaValidatorFactoryBuilder {
        private JsonProvider provider = JsonProvider.provider();
        private final Map<String, FormatValidator> customFormatValidators = new HashMap<>();
        private final SetMultimap<KeywordInfo<?>, KeywordValidatorCreator<?, ?>> factories = HashMultimap.create();

        public SchemaValidatorFactoryBuilder() {
            withCommonValidators();
            initCoreFormatValidators();
        }

        public <K extends SchemaKeyword, V extends KeywordValidator<K>> SchemaValidatorFactoryBuilder addValidator(KeywordInfo<K> keyword, KeywordValidatorCreator<K, V> factory) {
            factories.put(keyword, factory);
            return this;
        }

        public SchemaValidatorFactoryImpl build() {
            return new SchemaValidatorFactoryImpl(this.customFormatValidators, new KeywordValidatorCreators(this.factories), this.provider);
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

            this.addValidator(Keywords.TYPE, TypeValidator::new);
            this.addValidator(Keywords.ENUM, EnumValidator::new);
            this.addValidator(Keywords.NOT, NotKeywordValidator::new);
            this.addValidator(Keywords.CONST, ConstValidator::new);
            this.addValidator(Keywords.ALL_OF, AllOfValidator::new);
            this.addValidator(Keywords.ANY_OF, AnyOfValidator::new);
            this.addValidator(Keywords.ONE_OF, OneOfValidator::new);

            // ########################################################### //
            // #########  STRING VALIDATORS      ######################### //
            // ########################################################### //
            this.addValidator(Keywords.MAX_LENGTH, StringMaxLengthValidator::new);
            this.addValidator(Keywords.MIN_LENGTH, StringMinLengthValidator::new);
            this.addValidator(Keywords.PATTERN, StringPatternValidator::new);
            this.addValidator(Keywords.FORMAT, StringFormatValidator::new);

            // ########################################################### //
            // #########  ARRAY VALIDATORS      ######################### //
            // ########################################################### //

            this.addValidator(Keywords.MAX_ITEMS, ArrayMaxItemsValidator::new);
            this.addValidator(Keywords.MIN_ITEMS, ArrayMinItemsValidator::new);
            this.addValidator(Keywords.UNIQUE_ITEMS, ArrayUniqueItemsValidator::new);
            this.addValidator(Keywords.ITEMS, ArrayItemsValidator::getArrayItemsValidator);
            this.addValidator(Keywords.CONTAINS, ArrayContainsValidator::new);

            // ########################################################### //
            // #########  SCHEMA_NUMBER VALIDATORS      ######################### //
            // ########################################################### //
            this.addValidator(Keywords.MINIMUM, NumberLimitValidators::getMinValidator);
            this.addValidator(Keywords.MAXIMUM, NumberLimitValidators::getMaxValidator);
            this.addValidator(Keywords.MULTIPLE_OF, NumberMultipleOfValidator::new);

            // ########################################################### //
            // #########  OBJECT VALIDATORS      ######################### //
            // ########################################################### //
            this.addValidator(Keywords.PROPERTIES, PropertySchemaValidator::new);
            this.addValidator(Keywords.PROPERTY_NAMES, PropertyNameValidator::new);
            this.addValidator(Keywords.REQUIRED, RequiredPropertyValidator::new);
            this.addValidator(Keywords.PATTERN_PROPERTIES, PatternPropertiesValidator::new);
            this.addValidator(Keywords.ADDITIONAL_PROPERTIES, AdditionalPropertiesValidator::new);
            this.addValidator(Keywords.DEPENDENCIES, DependenciesValidator::new);
            this.addValidator(Keywords.MIN_PROPERTIES, MinPropertiesValidator::new);
            this.addValidator(Keywords.MAX_PROPERTIES, MaxPropertiesValidator::new);

            return this;
        }

        private void initCoreFormatValidators() {
            for (FormatType formatType : FormatType.values()) {
                customFormatValidators.put(formatType.toString(), forFormat(formatType));
            }
        }
    }

    /**
     * Static factory method for {@code FormatValidator} implementations supporting the
     * {@code getFormatName}s mandated by the json schema spec.
     * <p>
     * <ul>
     * <li>date-time</li>
     * <li>email</li>
     * <li>hostname</li>
     * <li>uri</li>
     * <li>ipv4</li>
     * <li>ipv6</li>
     * </ul>
     *
     * @param format one of the 6 built-in formats.
     * @return a {@code FormatValidator} implementation handling the {@code getFormatName} format.
     */
    static FormatValidator forFormat(final FormatType format) {
        requireNonNull(format, "format cannot be null");
        String formatName = format.toString();
        switch (formatName) {

            case "date-time":
                return new DateTimeFormatValidator();
            case "time":
                return new TimeFormatValidator();
            case "date":
                return new DateFormatValidator();
            case "email":
                return new EmailFormatValidator();
            case "hostname":
                return new HostnameFormatValidator();
            case "host-name":
                return new HostnameFormatValidator();
            case "uri":
                return new URIFormatValidator();
            case "ipv4":
                return new IPV4Validator();
            case "ip-address":
                return new IPV4Validator();
            case "ipv6":
                return new IPV6Validator();
            case "json-pointer":
                return new JsonPointerValidator();
            case "uri-template":
                return new URITemplateFormatValidator();
            case "uri-reference":
                return new URIReferenceFormatValidator();
            case "uriref":
                return new URIReferenceFormatValidator();
            case "style":
                return new NoopFormatValidator("style");
            case "color":
                return new ColorFormatValidator();
            case "phone":
                return new PhoneFormatValidator();
            case "regex":
                return new RegexFormatValidator();
            case "utc-millisec":
                return new PatternBasedValidator(Pattern.compile("^[0-9]+$"), "utc-millisex");
            default:
                throw new IllegalArgumentException("unsupported format: " + formatName);
        }
    }
}
