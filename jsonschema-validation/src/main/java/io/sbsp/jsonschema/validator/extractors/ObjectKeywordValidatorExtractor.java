package io.sbsp.jsonschema.validator.extractors;

import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.six.keywords.ObjectKeywords;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.extractors.KeywordValidators.KeywordValidatorsBuilder;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.keywords.AdditionalPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.MaxPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.MinPropertiesValidator;
import io.sbsp.jsonschema.validator.keywords.object.PatternPropertyValidator;
import io.sbsp.jsonschema.validator.keywords.object.PropertyDependenciesValidator;
import io.sbsp.jsonschema.validator.keywords.object.PropertyNameValidator;
import io.sbsp.jsonschema.validator.keywords.object.PropertySchemaValidator;
import io.sbsp.jsonschema.validator.keywords.object.RequiredPropertyValidator;
import io.sbsp.jsonschema.validator.keywords.SchemaDependenciesValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Collections.singleton;
import static javax.json.JsonValue.ValueType;

public class ObjectKeywordValidatorExtractor implements KeywordValidatorExtractor {

    private ObjectKeywordValidatorExtractor() {
    }

    public static ObjectKeywordValidatorExtractor objectKeywordsValidator() {
        return new ObjectKeywordValidatorExtractor();
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        return schema.hasObjectKeywords();
    }

    @Override
    public Set<ValueType> getApplicableTypes() {
        return singleton(ValueType.OBJECT);
    }

    public KeywordValidators getKeywordValidators(Schema schema, SchemaValidatorFactory factory) {
        KeywordValidatorsBuilder validators = KeywordValidators.builder()
                .schema(schema)
                .validatorFactory(factory);

        if (schema.hasObjectKeywords()) {
            ObjectKeywords keywords = schema.getObjectKeywords();

            // ########################################
            // PROPERTIES
            // ########################################
            if (keywords.getPropertySchemas().size() > 0) {
                ImmutableMap.Builder<String, SchemaValidator> propertyValidatorBuilder = ImmutableMap.builder();
                keywords.getPropertySchemas().forEach((prop, propSchema) -> {
                    propertyValidatorBuilder.put(prop, factory.createValidator(propSchema));
                });
                validators.addValidator(new PropertySchemaValidator(propertyValidatorBuilder.build(), schema));
            }

            // ########################################
            // PROPERTY NAMES
            // ########################################
            keywords.getPropertyNameSchema().ifPresent(propertyNameSchema -> {
                final SchemaValidator nameValidator = factory.createValidator(propertyNameSchema);
                validators.addValidator(PropertyNameValidator.builder()
                        .propertyNameValidator(nameValidator)
                        .jsonProvider(factory.getProvider())
                        .schema(schema)
                        .build());
            });

            // ########################################
            // REQUIRED
            // ########################################
            if (keywords.getRequiredProperties().size() > 0) {
                validators.addValidator(RequiredPropertyValidator.builder()
                        .schema(schema)
                        .requiredProperties(keywords.getRequiredProperties())
                        .build()
                );
            }

            // ########################################
            // PATTERN PROPERTIES
            // ########################################
            if (!keywords.getPatternProperties().isEmpty()) {
                Map<Pattern, SchemaValidator> patternValidators = new HashMap<>();
                keywords.getPatternProperties().forEach((pattern, patternSchema) ->
                        patternValidators.put(pattern, factory.createValidator(patternSchema)));

                validators.addValidator(PatternPropertyValidator.builder()
                        .schema(schema)
                        .patternValidators(patternValidators)
                        .build());
            }

            // ########################################
            // ADDITIONAL PROPS
            // ########################################

            keywords.getSchemaOfAdditionalProperties().ifPresent(schemaOfAdditionalProperties -> {
                SchemaValidator addtlPropsValidator = factory.createValidator(schemaOfAdditionalProperties);

                validators.addValidator(AdditionalPropertiesValidator.builder()
                        .additionalPropertiesValidator(addtlPropsValidator)
                        .patternProperties(keywords.getPatternProperties().keySet())
                        .propertySchemaKeys(keywords.getPropertySchemas().keySet())
                        .schema(schema)
                        .build());
            });

            // ########################################
            // SCHEMA DEPENDENCIES
            // ########################################
            if (!keywords.getSchemaDependencies().isEmpty()) {
                Map<String, SchemaValidator> dependencyValidators = new HashMap<>();
                keywords.getSchemaDependencies().forEach((key, depSchema) ->
                        dependencyValidators.put(key, factory.createValidator(depSchema)));

                validators.addValidator(SchemaDependenciesValidator.builder()
                        .schema(schema)
                        .dependencyValidators(dependencyValidators).build());
            }

            // ########################################
            // PROPERTY DEPENDENCIES
            // ########################################
            if (!keywords.getPropertyDependencies().isEmpty()) {
                validators.addValidator(PropertyDependenciesValidator.builder()
                        .schema(schema)
                        .propertyDependencies(keywords.getPropertyDependencies())
                        .build());
            }

            // ########################################
            // MIN SIZE
            // ########################################
            if (keywords.getMinProperties() != null) {
                int minProperties = keywords.getMinProperties();
                validators.addValidator(MinPropertiesValidator.builder()
                        .minProperties(minProperties)
                        .schema(schema)
                        .build());
            }

            // ########################################
            // MAX SIZE
            // ########################################
            if (keywords.getMaxProperties() != null) {
                int maxProperties = keywords.getMaxProperties();
                validators.addValidator(MaxPropertiesValidator.builder()
                        .schema(schema)
                        .maxProperties(maxProperties)
                        .build());
            }
        }
        return validators.build();
    }
}