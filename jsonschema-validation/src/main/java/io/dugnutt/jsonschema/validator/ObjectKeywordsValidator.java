package io.dugnutt.jsonschema.validator;

import com.google.common.base.Preconditions;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.ObjectKeywords;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import javax.json.JsonString;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DEPENDENCIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTY_NAMES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.REQUIRED;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;
import static java.util.Collections.emptyList;
import static javax.json.JsonValue.ValueType;

public class ObjectKeywordsValidator implements PartialSchemaValidator {
    private ObjectKeywordsValidator() {
    }

    public static ObjectKeywordsValidator objectKeywordsValidator() {
        return new ObjectKeywordsValidator();
    }

    @Override
    public boolean appliesToSchema(JsonSchema schema) {
        return schema.getObjectKeywords().isPresent();
    }

    @Override
    public boolean appliesToValue(PathAwareJsonValue value) {
        return value.is(ValueType.OBJECT);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject, JsonSchema schema, SchemaValidatorFactory factory) {
        Preconditions.checkArgument(subject.is(ValueType.OBJECT), "Requires JsonArray as input");
        ObjectKeywords keywords = schema.getObjectKeywords()
                .orElseThrow(() -> new IllegalArgumentException("Schema must have object keywords"));

        Helper helper = new Helper(subject, schema, keywords, factory);
        List<ValidationError> failures = new ArrayList<>();

        // These are nested by nature
        failures.addAll(helper.testProperties(subject));

        failures.addAll(helper.testRequiredProperties(subject));

        failures.addAll(helper.testPropertyDependencies(subject));
        failures.addAll(helper.testSchemaDependencies(subject));
        failures.addAll(helper.testPatternProperties(subject));

        helper.testAdditionalProperties(subject).ifPresent(failures::add);
        helper.testSize(subject).ifPresent(failures::add);
        return ValidationError.collectErrors(schema, subject.getPath(), failures);
    }

    private static class Helper {
        private final JsonSchema schema;
        private final ObjectKeywords keywords;
        private final SchemaValidatorFactory factory;

        public Helper(PathAwareJsonValue subject, JsonSchema schema, ObjectKeywords keywords, SchemaValidatorFactory factory) {
            this.schema = checkNotNull(schema);
            this.keywords = checkNotNull(keywords);
            this.factory = checkNotNull(factory);
        }

        private Optional<ValidationError> testAdditionalProperties(final PathAwareJsonValue subject) {
            final List<ValidationError> additionalPropertyErrors = new ArrayList<>();
            keywords.getSchemaOfAdditionalProperties()
                    .map(factory::createValidator)
                    .ifPresent(extraPropertyValidator ->
                            keywords.getAdditionalProperties(subject)
                                    .forEach(propertyName -> {
                                        PathAwareJsonValue propertyValue = subject.getPathAware(propertyName);
                                        Optional<ValidationError> error = extraPropertyValidator.validate(propertyValue);
                                        error.ifPresent(additionalPropertyErrors::add);
                                    })

                    );

            if (additionalPropertyErrors.size() > 0) {
                return buildKeywordFailure(subject, schema, ADDITIONAL_PROPERTIES)
                        .message("Additional properties were invalid")
                        .causingExceptions(additionalPropertyErrors)
                        .buildOptional();
            }
            return Optional.empty();
        }

        private List<ValidationError> testPatternProperties(final PathAwareJsonValue subject) {
            Set<String> subjectProperties = subject.propertyNames();
            if (subjectProperties.isEmpty()) {
                return emptyList();
            }
            List<ValidationError> allErrors = new ArrayList<>();
            keywords.getPatternProperties().forEach((pattern, patternSchema) -> {
                subjectProperties.stream()
                        .filter(regexMatches(pattern))
                        .forEach(propertyName -> {
                            final PathAwareJsonValue propertyValue = subject.getPathAware(propertyName);
                            final Optional<ValidationError> error = factory.createValidator(patternSchema)
                                    .validate(propertyValue);
                            error.ifPresent(allErrors::add);
                        });
            });
            return allErrors;
        }

        private Predicate<String> regexMatches(Pattern regex) {
            checkNotNull(regex, "regex must not be null");
            return string -> regex.matcher(string).find();
        }

        private List<ValidationError> testProperties(final PathAwareJsonValue subject) {
            final List<ValidationError> propertyErrors = new ArrayList<>();
            final List<ValidationError> propertyNameErrors = new ArrayList<>();

            final Optional<JsonSchemaValidator> propertyNameValidator = keywords.getPropertyNameSchema().map(factory::createValidator);

            subject.forEachKey((propertyName, pathAwareProperty) -> {
                // Validate against property schema if one exists
                keywords.findPropertySchema(propertyName)
                        .ifPresent(propertySchema -> {
                            final JsonSchema validateSchema;
                            if (propertySchema.getRefURI() != null) {
                                // validateSchema = propertySchema.getFullyDereferencedSchema()
                                //         .orElse(propertySchema);
                                throw new UnsupportedOperationException();
                            } else {
                                validateSchema = propertySchema;
                            }
                            factory.createValidator(validateSchema)
                                    .validate(pathAwareProperty)
                                    .ifPresent(propertyErrors::add);
                        });

                // Validate against property name schema if one exists
                propertyNameValidator.ifPresent(validator -> {
                    //The validators work against json objects, not raw java objects, so we need to
                    //wrap this in a JsonString here.
                    final JsonString jsonValue = this.factory.getProvider().createValue(propertyName);
                    validator.validate(pathAwareProperty.withValue(jsonValue))
                            .ifPresent(propertyNameErrors::add);
                });
            });

            if (propertyNameErrors.size() > 0) {
                propertyErrors.add(
                        buildKeywordFailure(subject, schema, PROPERTY_NAMES)
                                .message("Invalid property names")
                                .causingExceptions(propertyNameErrors)
                                .build()
                );
            }

            return propertyErrors;
        }

        private List<ValidationError> testPropertyDependencies(final PathAwareJsonValue subject) {
            return keywords.getPropertyDependencies().keySet().stream()
                    .filter(subject::containsKey)
                    .flatMap(this::getDependenciesForProperty)
                    .filter(mustBePresent -> !subject.containsKey(mustBePresent))
                    .map(missingKey ->
                            buildKeywordFailure(subject, schema, DEPENDENCIES)
                                    .message("property [%s] is required", missingKey)
                                    .build())
                    .collect(Collectors.toList());
        }

        private Stream<String> getDependenciesForProperty(String forProperty) {
            return keywords.getPropertyDependencies().get(forProperty).stream();
        }

        private List<ValidationError> testRequiredProperties(final PathAwareJsonValue subject) {
            return keywords.getRequiredProperties().stream()
                    .filter(key -> !subject.containsKey(key))
                    .map(missingKey ->
                            buildKeywordFailure(subject, schema, REQUIRED)
                                    .message("required key [%s] not found", missingKey)
                                    .build())
                    .collect(Collectors.toList());
        }

        private List<ValidationError> testSchemaDependencies(final PathAwareJsonValue subject) {
            List<ValidationError> errors = new ArrayList<>();
            keywords.getSchemaDependencies().forEach((propName, schema) -> {
                if (subject.containsKey(propName)) {
                    factory.createValidator(schema)
                            .validate(subject)
                            .ifPresent(errors::add);
                }
            });
            return errors;
        }

        private Optional<ValidationError> testSize(final PathAwareJsonValue subject) {
            int actualSize = subject.numberOfProperties();
            if (keywords.getMinProperties() != null && actualSize < keywords.getMinProperties()) {
                return buildKeywordFailure(subject, schema, MIN_PROPERTIES)
                        .message("minimum size: [%d], found: [%d]", keywords.getMinProperties(), actualSize)
                        .buildOptional();
            }
            if (keywords.getMaxProperties() != null && actualSize > keywords.getMaxProperties()) {
                return buildKeywordFailure(subject, schema, MAX_PROPERTIES)
                        .message("maximum size: [%d], found: [%d]", keywords.getMaxProperties(), actualSize)
                        .buildOptional();
            }

            return Optional.empty();
        }
    }
}
