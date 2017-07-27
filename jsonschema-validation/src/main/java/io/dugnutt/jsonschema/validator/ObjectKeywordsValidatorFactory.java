package io.dugnutt.jsonschema.validator;

import com.google.common.collect.ImmutableMap;
import io.dugnutt.jsonschema.six.ObjectKeywords;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DEPENDENCIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PATTERN_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTY_NAMES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.REQUIRED;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;
import static java.util.Collections.singleton;
import static javax.json.JsonValue.ValueType;

public class ObjectKeywordsValidatorFactory implements PartialValidatorFactory {

    private ObjectKeywordsValidatorFactory() {
    }

    public static ObjectKeywordsValidatorFactory objectKeywordsValidator() {
        return new ObjectKeywordsValidatorFactory();
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        return schema.hasObjectKeywords();
    }

    @Override
    public Set<ValueType> appliesToTypes() {
        return singleton(ValueType.OBJECT);
    }

    public SchemaValidator forSchema(Schema schema, SchemaValidatorFactory factory) {
        if (schema.getObjectKeywords().isPresent()) {
            ObjectKeywords keywords = schema.getObjectKeywords().get();
            ChainedValidator.ChainedValidatorBuilder validator = ChainedValidator.builder()
                    .schema(schema)
                    .factory(factory);

            // ########################################
            // PROPERTIES
            // ########################################
            if (keywords.getPropertySchemas().size() > 0) {
                ImmutableMap.Builder<String, SchemaValidator> propertyValidatorBuilder = ImmutableMap.builder();
                keywords.getPropertySchemas().forEach((prop, propSchema)->{
                    propertyValidatorBuilder.put(prop, factory.createValidator(propSchema));
                });
                final ImmutableMap<String, SchemaValidator> propertyValidators = propertyValidatorBuilder.build();
                validator.addValidator(PROPERTIES, (subject, parentReport) -> {
                    final Set<String> subjectProperties = subject.asJsonObject().keySet();
                    ValidationReport report = new ValidationReport();

                    for (Map.Entry<String, SchemaValidator> propertySchemas : propertyValidators.entrySet()) {
                        final String propertyName = propertySchemas.getKey();
                        if (subjectProperties.contains(propertyName)) {
                            SchemaValidator propValidator = propertySchemas.getValue();
                            PathAwareJsonValue pathAwareSubject = subject.getPathAware(propertyName);
                            propValidator.validate(pathAwareSubject, report);
                        }
                    }

                    return parentReport.addReport(schema, subject, report);
                });
            }

            // ########################################
            // PROPERTY NAMES
            // ########################################
            keywords.getPropertyNameSchema().map(factory::createValidator)
                    .ifPresent(nameValidator -> {
                        validator.addValidator(PROPERTY_NAMES, ((subject, parentReport) -> {
                            ValidationReport report = new ValidationReport();
                            final Set<String> subjectProperties = subject.asJsonObject().keySet();
                            for (String subjectProperty : subjectProperties) {
                                JsonString value = factory.getProvider().createValue(subjectProperty);
                                nameValidator.validate(new PathAwareJsonValue(value, subject.getPath()), report);
                            }

                            List<ValidationError> errors = report.getErrors();
                            if (!errors.isEmpty()) {
                                return parentReport.addError(buildKeywordFailure(subject, schema, PROPERTY_NAMES)
                                        .message("Invalid property names")
                                        .causingExceptions(errors)
                                        .build());
                            }
                            return true;
                        }));
                    });

            // ########################################
            // REQUIRED
            // ########################################

            if (!keywords.getRequiredProperties().isEmpty()) {
                validator.addValidator(REQUIRED, (subject, report) ->
                        !keywords.getRequiredProperties().stream()
                                .filter(key -> !subject.containsKey(key))
                                .map(missingProperty -> report.addError(buildKeywordFailure(subject, schema, REQUIRED)
                                        .message("required key [%s] not found", missingProperty)
                                        .build()))
                                .findAny()
                                .isPresent());
            }

            // ########################################
            // PATTERN PROPERTIES
            // ########################################
            if (!keywords.getPatternProperties().isEmpty()) {
                Map<Pattern, SchemaValidator> patternValidators = new HashMap<>();
                // Build validators now
                keywords.getPatternProperties().forEach((pattern, patternSchema) ->
                        patternValidators.put(pattern, factory.createValidator(patternSchema)));

                validator.addValidator(PATTERN_PROPERTIES, (subject, parentReport) -> {
                    Set<String> subjectProperties = subject.propertyNames();
                    if (subjectProperties.isEmpty()) {
                        return true;
                    }
                    boolean success = true;
                    ValidationReport report = new ValidationReport();
                    for (Map.Entry<Pattern, SchemaValidator> patternValidatorEntries : patternValidators.entrySet()) {
                        Pattern pattern = patternValidatorEntries.getKey();
                        SchemaValidator patternValidator = patternValidatorEntries.getValue();
                        for (String propertyName : subjectProperties) {
                            if (pattern.matcher(propertyName).matches()) {
                                final PathAwareJsonValue propertyValue = subject.getPathAware(propertyName);
                                success = success && patternValidator.validate(propertyValue, report);
                            }
                        }
                    }
                    return parentReport.addReport(schema, subject, report);
                });
            }

            // ########################################
            // ADDITIONAL PROPS
            // ########################################

            keywords.getSchemaOfAdditionalProperties().ifPresent(schemaOfAdditionalProperties -> {
                SchemaValidator addtlPropsValidator = factory.createValidator(schema);

                validator.addValidator(ADDITIONAL_PROPERTIES, (subject, parentReport) -> {
                    ValidationReport report = new ValidationReport();

                    boolean success = true;
                    for (String propertyName : keywords.getAdditionalProperties(subject)) {
                        PathAwareJsonValue propertyValue = subject.getPathAware(propertyName);
                        success = success && addtlPropsValidator.validate(propertyValue, report);
                    }
                    if (!success) {
                        parentReport.addReport(schema, subject, report);
                    }
                    return success;

                });
            });

            // ########################################
            // SCHEMA DEPENDENCIES
            // ########################################
            List<ValidationError> errors = new ArrayList<>();
            if (!keywords.getSchemaDependencies().isEmpty()) {
                Map<String, SchemaValidator> dependencyValidators = new HashMap<>();
                keywords.getSchemaDependencies().forEach((key, depSchema) ->
                        dependencyValidators.put(key, factory.createValidator(depSchema)));

                validator.addValidator(DEPENDENCIES, (subject, report) -> {
                    boolean success = true;
                    for (Map.Entry<String, SchemaValidator> dependencyValidatorEntries : dependencyValidators.entrySet()) {
                        String propName = dependencyValidatorEntries.getKey();
                        SchemaValidator dependencyValidator = dependencyValidatorEntries.getValue();
                        if (subject.containsKey(propName)) {
                            success = success && dependencyValidator.validate(subject, report);
                        }
                    }
                    return success;
                });
            }

            // ########################################
            // PROPERTY DEPENDENCIES
            // ########################################
            if (!keywords.getPropertyDependencies().isEmpty()) {
                validator.addValidator(DEPENDENCIES, (subject, report) -> {
                    boolean success = true;
                    for (Map.Entry<String, String> dependency : keywords.getPropertyDependencies().entries()) {
                        String ifThisPropertyExists = dependency.getKey();
                        String thenThisMustAlsoExist = dependency.getValue();
                        if (subject.containsKey(ifThisPropertyExists) && !subject.containsKey(thenThisMustAlsoExist)) {
                            success = success && report.addError(buildKeywordFailure(subject, schema, DEPENDENCIES)
                                    .message("property [%s] is required because [%s] was present", thenThisMustAlsoExist, ifThisPropertyExists)
                                    .build());
                        }
                    }
                    return success;
                });

                // ########################################
                // MIN SIZE
                // ########################################
                if (keywords.getMinProperties() != null) {
                    int minProperties = keywords.getMinProperties();
                    validator.addValidator(MIN_PROPERTIES, (subject, report) -> {
                        int actualSize = subject.numberOfProperties();
                        if (actualSize < minProperties) {
                            return report.addError(buildKeywordFailure(subject, schema, MIN_PROPERTIES)
                                    .message("minimum size: [%d], found: [%d]", minProperties, actualSize)
                                    .build());
                        }
                        return true;
                    });
                }

                // ########################################
                // MAX SIZE
                // ########################################
                if (keywords.getMaxProperties() != null) {
                    int maxProperties = keywords.getMaxProperties();
                    validator.addValidator(MAX_PROPERTIES, (subject, report) -> {
                        int actualSize = subject.numberOfProperties();
                        if (actualSize > maxProperties) {
                            return report.addError(buildKeywordFailure(subject, schema, MAX_PROPERTIES)
                                    .message("maximum size: [%d], found: [%d]", maxProperties, actualSize)
                                    .build());
                        }
                        return true;
                    });
                }
            }

            return validator.build();
        } else {
            return SchemaValidator.NOOP_VALIDATOR;
        }
    }
}
