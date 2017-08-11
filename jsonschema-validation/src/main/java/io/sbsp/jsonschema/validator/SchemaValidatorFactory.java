package io.sbsp.jsonschema.validator;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.FormatType;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.utils.StreamUtils;
import io.sbsp.jsonschema.validator.factory.KeywordToValidatorTransformer;
import io.sbsp.jsonschema.validator.keywords.AdditionalPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.AllOfValidator;
import io.sbsp.jsonschema.validator.keywords.AnyOfValidator;
import io.sbsp.jsonschema.validator.keywords.ConstValidator;
import io.sbsp.jsonschema.validator.keywords.EnumValidator;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import io.sbsp.jsonschema.validator.keywords.NotKeywordValidator;
import io.sbsp.jsonschema.validator.keywords.OneOfValidator;
import io.sbsp.jsonschema.validator.keywords.SchemaDependenciesValidator;
import io.sbsp.jsonschema.validator.keywords.TypeValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayContainsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayItemValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayMaxItemsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayMinItemsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayPerItemValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayUniqueItemsValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberExclusiveMaximumValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberExclusiveMinimumValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberMaximumValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberMinimumValidator;
import io.sbsp.jsonschema.validator.keywords.number.NumberMultipleOfValidator;
import io.sbsp.jsonschema.validator.keywords.object.MaxPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.MinPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.PatternPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.PatternPropertiesValidator.PatternPropertiesValidatorBuilder;
import io.sbsp.jsonschema.validator.keywords.object.PropertyDependenciesValidator;
import io.sbsp.jsonschema.validator.keywords.object.PropertyNameValidator;
import io.sbsp.jsonschema.validator.keywords.object.PropertySchemaValidator;
import io.sbsp.jsonschema.validator.keywords.object.RequiredPropertyValidator;
import io.sbsp.jsonschema.validator.keywords.string.StringFormatValidator;
import io.sbsp.jsonschema.validator.keywords.string.StringMaxLengthValidator;
import io.sbsp.jsonschema.validator.keywords.string.StringMinLengthValidator;
import io.sbsp.jsonschema.validator.keywords.string.StringPatternValidator;
import io.sbsp.jsonschema.validator.keywords.string.formatValidators.FormatValidator;
import lombok.Builder;
import lombok.NonNull;

import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Builder(builderClassName = "Builder", toBuilder = true)
public class SchemaValidatorFactory {

    public static final SchemaValidatorFactory DEFAULT_VALIDATOR_FACTORY = builder().build();

    private final Map<URI, SchemaValidator> validatorCache = new HashMap<>();

    @NonNull
    private final Map<String, FormatValidator> customFormatValidators;

    @NonNull
    private final SetMultimap<KeywordMetadata, KeywordToValidatorTransformer> validators;

    private final JsonProvider provider;

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

    public static class Builder {
        public Builder() {
            this.provider = JsonProvider.provider();
            this.customFormatValidators = new HashMap<>();
            this.validators = HashMultimap.create();

            withCommonValidators();
            initCoreFormatValidators();
        }

        public Builder customFormatValidator(String format, FormatValidator formatValidator) {
            checkArgument(!Strings.isNullOrEmpty(format), "format must not be blank");
            checkNotNull(formatValidator, "formatValidator must not be null");
            customFormatValidators.put(format, formatValidator);
            return this;
        }

        public <K extends SchemaKeyword> Builder registerValidator(KeywordMetadata<K> keyword, KeywordToValidatorTransformer<K, ? extends KeywordValidator<K>> validatorTx) {
            validators.put(keyword, validatorTx);
            return this;
        }

        public Builder withCommonValidators() {

            // ########################################
            // TYPE
            // ########################################
            this.registerValidator(SchemaKeyword.type, (schema, keywordInfo, keyword, factory) ->
                    TypeValidator.builder()
                            .schema(schema)
                            .requiredTypes(keyword.getTypes())
                            .build());

            // ########################################
            // ENUM
            // ########################################
            this.registerValidator(SchemaKeyword.$enum, (schema, keywordInfo, keyword, factory) ->
                    EnumValidator.builder()
                            .schema(schema)
                            .enumValues(keyword.getKeywordValue())
                            .build());

            // ########################################
            // NOT
            // ########################################
            registerValidator(SchemaKeyword.not, (schema, keywordInfo, keyword, factory) ->
                    NotKeywordValidator.builder()
                            .schema(schema)
                            .notSchema(schema)
                            .notValidator(factory.createValidator(schema))
                            .build());

            // ########################################
            // CONST
            // ########################################
            registerValidator(SchemaKeyword.$const, (schema, keywordInfo, keyword, factory) ->
                    ConstValidator.builder()
                            .parentSchema(schema)
                            .constValue(keyword.getKeywordValue())
                            .build());

            // ########################################
            // ALL OF
            // ########################################
            registerValidator(SchemaKeyword.allOf, (schema, keywordInfo, keyword, factory) -> {
                final List<Schema> allOfSchemas = keyword.getSchemas();
                List<SchemaValidator> allOfValidators = allOfSchemas.stream()
                        .map(factory::createValidator)
                        .collect(StreamUtils.toImmutableList());

                return AllOfValidator.builder()
                        .schema(schema)
                        .allOfValidators(allOfValidators)
                        .build();
            });

            // ########################################
            // ANY OF
            // ########################################
            registerValidator(SchemaKeyword.anyOf, (schema, keywordInfo, keyword, factory) -> {
                final List<Schema> anyOfSchemas = keyword.getSchemas();
                List<SchemaValidator> anyOfValidators = anyOfSchemas.stream()
                        .map(factory::createValidator)
                        .collect(StreamUtils.toImmutableList());

                return AnyOfValidator.builder()
                        .schema(schema)
                        .anyOfValidators(anyOfValidators)
                        .build();
            });

            // ########################################
            // ONE OF
            // ########################################
            registerValidator(SchemaKeyword.oneOf, (schema, keywordInfo, keyword, factory) -> {
                final List<Schema> oneOfSchemas = keyword.getSchemas();
                List<SchemaValidator> oneOfValidators = oneOfSchemas.stream()
                        .map(factory::createValidator)
                        .collect(StreamUtils.toImmutableList());

                return OneOfValidator.builder()
                        .schema(schema)
                        .oneOfValidators(oneOfValidators)
                        .build();
            });

            // ########################################
            // MAX_LENGTH
            // ########################################

            registerValidator(SchemaKeyword.maxLength, (schema, keywordInfo, keyword, factory) ->
                    StringMaxLengthValidator.builder()
                            .schema(schema)
                            .maxLength(keyword.getInteger())
                            .build());

            // ########################################
            // MIN_LENGTH
            // ########################################

            registerValidator(SchemaKeyword.minLength, (schema, keywordInfo, keyword, factory) ->
                    StringMinLengthValidator.builder()
                            .schema(schema)
                            .minLength(keyword.getInteger())
                            .build());

            // ########################################
            // PATTERN
            // ########################################

            registerValidator(SchemaKeyword.pattern, (schema, keywordInfo, keyword, factory) ->
                    StringPatternValidator.builder()
                            .schema(schema)
                            .pattern(Pattern.compile(keyword.getKeywordValue()))
                            .build());

            // ########################################
            // FORMAT
            // ########################################

            registerValidator(SchemaKeyword.format, (schema, keywordInfo, keyword, factory) -> {
                final String formatValue = keyword.getKeywordValue();
                return factory.getFormatValidator(formatValue)
                        .map(formatValidator -> StringFormatValidator.builder()
                                .schema(schema)
                                .formatValidator(formatValidator)
                                .build()).orElse(null);
            });

            // ########################################
            // MIN ITEMS
            // ########################################

            registerValidator(SchemaKeyword.minItems, (schema, keywordInfo, keyword, factory) ->
                    ArrayMinItemsValidator.builder()
                            .minItems(keyword.getInteger())
                            .schema(schema).build());

            // ########################################
            // MAX ITEMS
            // ########################################

            registerValidator(SchemaKeyword.maxItems, (schema, keywordInfo, keyword, factory) ->
                    ArrayMaxItemsValidator.builder()
                            .maxItems(keyword.getInteger())
                            .schema(schema)
                            .build());

            // ########################################
            // UNIQUE ITEMS
            // ########################################

            registerValidator(SchemaKeyword.uniqueItems, (schema, keywordInfo, keyword, factory) ->
                    ArrayUniqueItemsValidator.builder()
                            .schema(schema)
                            .build()
            );

            // ########################################
            // ALL ITEMS
            // ########################################

            registerValidator(SchemaKeyword.items, (schema, keywordInfo, keyword, factory) -> {
                if (keyword.getAllItemSchema().isPresent()) {
                    final SchemaValidator allItemValidator = factory.createValidator(keyword.getAllItemSchema().get());
                    return ArrayItemValidator.builder()
                            .allItemValidator(allItemValidator)
                            .parentSchema(schema)
                            .build();
                } else {
                    final SchemaValidator additionItemValidator = keyword.getAdditionalItemSchema()
                            .map(factory::createValidator)
                            .orElse(null);
                    final List<SchemaValidator> indexedValidators = factory.createValidators(keyword.getIndexedSchemas());
                    return ArrayPerItemValidator.builder()
                            .schema(schema)
                            .indexedValidators(indexedValidators)
                            .additionalItemValidator(additionItemValidator)
                            .build();
                }
            });

            // ########################################
            // CONTAINS SCHEMA
            // ########################################

            registerValidator(SchemaKeyword.contains, (schema, keywordInfo, keyword, factory) -> {
                final SchemaValidator containsValidator = factory.createValidator(keyword.getSchema());
                return ArrayContainsValidator.builder()
                        .schema(schema)
                        .containsValidator(containsValidator)
                        .build();
            });

            // ########################################
            // MINIMUM
            // ########################################

            registerValidator(SchemaKeyword.minimum, (schema, keywordInfo, keyword, factory) -> {
                if (keyword.isExclusive()) {
                    return NumberExclusiveMinimumValidator.builder()
                            .schema(schema)
                            .exclusiveMinimum(keyword.getMinimum().doubleValue())
                            .build();
                } else {
                    return NumberMinimumValidator.builder()
                            .schema(schema)
                            .minimum(keyword.getMinimum().doubleValue())
                            .build();
                }
            });

            // ########################################
            // MAXIMUM
            // ########################################

            registerValidator(SchemaKeyword.maximum, (schema, keywordInfo, keyword, factory) -> {
                if (keyword.isExclusive()) {
                    return NumberExclusiveMaximumValidator.builder()
                            .schema(schema)
                            .exclusiveMaximum(keyword.getMaximum().doubleValue())
                            .build();
                } else {
                    return NumberMaximumValidator.builder()
                            .schema(schema)
                            .maximum(keyword.getMaximum().doubleValue())
                            .build();
                }
            });

            // ########################################
            // MULTIPLE OF
            // ########################################

            registerValidator(SchemaKeyword.multipleOf, (schema, keywordInfo, keyword, factory) ->
                    NumberMultipleOfValidator.builder()
                            .schema(schema)
                            .multipleOf(keyword.getDouble())
                            .build());

            // ########################################
            // PROPERTIES
            // ########################################
            registerValidator(SchemaKeyword.properties, (schema, keywordInfo, keyword, factory) -> {
                ImmutableMap.Builder<String, SchemaValidator> propertyValidatorBuilder = ImmutableMap.builder();
                keyword.getSchemas().forEach((prop, propSchema) -> {
                    propertyValidatorBuilder.put(prop, factory.createValidator(propSchema));
                });
                return new PropertySchemaValidator(propertyValidatorBuilder.build(), schema);
            });

            // ########################################
            // PROPERTY NAMES
            // ########################################
            registerValidator(SchemaKeyword.propertyNames, (schema, keywordInfo, keyword, factory) -> {
                final SchemaValidator nameValidator = factory.createValidator(keyword.getSchema());
                return PropertyNameValidator.builder()
                        .propertyNameValidator(nameValidator)
                        .jsonProvider(factory.getProvider())
                        .schema(schema)
                        .build();
            });

            // ########################################
            // REQUIRED
            // ########################################
            registerValidator(SchemaKeyword.required, (schema, keywordInfo, keyword, factory) ->
                    RequiredPropertyValidator.builder()
                            .schema(schema)
                            .requiredProperties(keyword.getStringSet())
                            .build()
            );

            // ########################################
            // PATTERN PROPERTIES
            // ########################################
            registerValidator(SchemaKeyword.patternProperties, (schema, keywordInfo, keyword, factory) -> {
                final Map<String, Schema> patternSchemas = keyword.getSchemas();
                final PatternPropertiesValidatorBuilder builder = PatternPropertiesValidator.builder();
                patternSchemas.forEach((pattern, patternSchema) ->
                        builder.addPatternValidator(pattern, factory.createValidator(patternSchema)));

                return builder
                        .schema(schema)
                        .build();
            });

            // ########################################
            // ADDITIONAL PROPS
            // ########################################

            registerValidator(SchemaKeyword.additionalProperties, (schema, keywordInfo, keyword, factory) -> {
                final SchemaValidator addtlPropValidator = factory.createValidator(keyword.getSchema());
                final Draft6Schema draft6Schema = schema.asDraft6();
                return AdditionalPropertiesValidator.builder()
                        .additionalPropertiesValidator(addtlPropValidator)
                        .patternProperties(draft6Schema.getPatternProperties().keySet())
                        .propertySchemaKeys(draft6Schema.getProperties().keySet())
                        .schema(schema)
                        .build();
            });

            // ########################################
            // SCHEMA DEPENDENCIES
            // ########################################

            registerValidator(SchemaKeyword.dependencies, (schema, keywordInfo, keyword, factory) -> {
                Map<String, SchemaValidator> dependencyValidators = new HashMap<>();
                keyword.getDependencySchemas().getSchemas().forEach((key, depSchema) ->
                        dependencyValidators.put(key, factory.createValidator(depSchema)));

                return SchemaDependenciesValidator.builder()
                        .schema(schema)
                        .dependencyValidators(dependencyValidators).build();
            });

            // ########################################
            // PROPERTY DEPENDENCIES
            // ########################################

            registerValidator(SchemaKeyword.dependencies, (schema, keywordInfo, keyword, factory) ->
                    PropertyDependenciesValidator.builder()
                            .schema(schema)
                            .propertyDependencies(keyword.getPropertyDependencies())
                            .build());

            // ########################################
            // MIN PROPERTIES
            // ########################################

            registerValidator(SchemaKeyword.minProperties, (schema, keywordInfo, keyword, factory) ->
                    MinPropertiesValidator.builder()
                            .minProperties(keyword.getInteger())
                            .schema(schema)
                            .build()
            );

            // ########################################
            // MAX PROPERTIES
            // ########################################

            registerValidator(SchemaKeyword.maxProperties, (schema, keywordInfo, keyword, factory) ->
                    MaxPropertiesValidator.builder()
                            .maxProperties(keyword.getInteger())
                            .schema(schema)
                            .build()
            );

            return this;
        }
        // Set<JsonSchemaType> getTypes();
        //
        // Optional<JsonArray> getEnumValues();
        //
        // Optional<JsonValue> getDefaultValue();
        //
        // String getFormat();
        //
        // Integer getMinLength();
        //
        // Integer getMaxLength();
        //
        // String getPattern();
        //
        // Number getMaximum();
        //
        // Number getMinimum();
        //
        // Integer getMinItems();
        //
        // Integer getMaxItems();
        //
        // Optional<D> getAllItemSchema();
        //
        // List<Schema> getItemSchemas();
        //
        // Optional<D> getAdditionalItemsSchema();
        //
        // Map<String, Schema> getProperties();
        //
        // Map<String, Schema> getPatternProperties();
        //
        // Optional<D> getAdditionalPropertiesSchema();
        //
        // SetMultimap<String, String> getPropertyDependencies();
        //
        // Map<String, Schema> getPropertySchemaDependencies();
        //
        // boolean requiresUniqueItems();

        private void initCoreFormatValidators() {
            for (FormatType formatType : FormatType.values()) {
                customFormatValidators.put(formatType.toString(), FormatValidator.forFormat(formatType));
            }
        }
    }
}
