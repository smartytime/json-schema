package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonString;
import javax.json.JsonValue;
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
import static java.util.Collections.emptyList;

public class ObjectSchemaValidator extends SchemaValidator<ObjectSchema> {

    public ObjectSchemaValidator(ObjectSchema schema) {
        super(schema);
    }

    public ObjectSchemaValidator(ObjectSchema schema, SchemaValidatorFactory context) {
        super(schema, context);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject) {

        Optional<ValidationError> wrongType = verifyType(subject, JsonSchemaType.OBJECT, schema.isRequiresObject());
        if (wrongType.isPresent()) {
            return wrongType;
        }

        if (subject.is(JsonValue.ValueType.OBJECT)) {
            List<ValidationError> failures = new ArrayList<>();

            // These are nested by nature
            failures.addAll(testProperties(subject));

            failures.addAll(testRequiredProperties(subject));

            failures.addAll(testPropertyDependencies(subject));
            failures.addAll(testSchemaDependencies(subject));
            failures.addAll(testPatternProperties(subject));

            testAdditionalProperties(subject).ifPresent(failures::add);
            testSize(subject).ifPresent(failures::add);
            return ValidationError.collectErrors(schema, subject.getPath(), failures);
        }
        return Optional.empty();
    }

    private Optional<ValidationError> testAdditionalProperties(final PathAwareJsonValue subject) {
        final List<ValidationError> additionalPropertyErrors = new ArrayList<>();
        schema.getSchemaOfAdditionalProperties()
                .map(factory::createValidator)
                .ifPresent(extraPropertyValidator ->
                        schema.getAdditionalProperties(subject)
                                .forEach(propertyName -> {
                                    PathAwareJsonValue propertyValue = subject.get(propertyName);
                                    Optional<ValidationError> error = extraPropertyValidator.validate(propertyValue);
                                    error.ifPresent(additionalPropertyErrors::add);
                                })

                );

        if (additionalPropertyErrors.size() > 0) {
            return buildKeywordFailure(subject, ADDITIONAL_PROPERTIES)
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
        schema.getPatternProperties().forEach((pattern, patternSchema) -> {
            subjectProperties.stream()
                    .filter(regexMatches(pattern))
                    .forEach(propertyName -> {
                        final PathAwareJsonValue propertyValue = subject.get(propertyName);
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

        final Optional<SchemaValidator<Schema>> propertyNameValidator = schema.getPropertyNameSchema().map(factory::createValidator);

        subject.forEach((propertyName, pathAwareProperty) -> {
            // Validate against property schema if one exists
            schema.findPropertySchema(propertyName)
                    .ifPresent(propertySchema -> {
                        final Schema validateSchema;
                        if (propertySchema instanceof ReferenceSchema) {
                            validateSchema = ((ReferenceSchema) propertySchema).getFullyDereferencedSchema()
                                    .orElse(propertySchema);
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
                    this.buildKeywordFailure(subject, PROPERTY_NAMES)
                            .message("Invalid property names")
                            .causingExceptions(propertyNameErrors)
                            .build()
            );
        }

        return propertyErrors;
    }

    private List<ValidationError> testPropertyDependencies(final PathAwareJsonValue subject) {
        return schema.getPropertyDependencies().keySet().stream()
                .filter(subject::containsKey)
                .flatMap(this::getDependenciesForProperty)
                .filter(mustBePresent -> !subject.containsKey(mustBePresent))
                .map(missingKey ->
                        buildKeywordFailure(subject, DEPENDENCIES)
                                .message("property [%s] is required", missingKey)
                                .build())
                .collect(Collectors.toList());
    }

    private Stream<String> getDependenciesForProperty(String forProperty) {
        return schema.getPropertyDependencies().get(forProperty).stream();
    }

    private List<ValidationError> testRequiredProperties(final PathAwareJsonValue subject) {
        return schema.getRequiredProperties().stream()
                .filter(key -> !subject.containsKey(key))
                .map(missingKey ->
                        buildKeywordFailure(subject, REQUIRED)
                                .message("required key [%s] not found", missingKey)
                                .build())
                .collect(Collectors.toList());
    }

    private List<ValidationError> testSchemaDependencies(final PathAwareJsonValue subject) {
        List<ValidationError> errors = new ArrayList<>();
        schema.getSchemaDependencies().forEach((propName, schema) -> {
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
        if (schema.getMinProperties() != null && actualSize < schema.getMinProperties()) {
            return buildKeywordFailure(subject, MIN_PROPERTIES)
                    .message("minimum size: [%d], found: [%d]", schema.getMinProperties(), actualSize)
                    .buildOptional();
        }
        if (schema.getMaxProperties() != null && actualSize > schema.getMaxProperties()) {
            return buildKeywordFailure(subject, MAX_PROPERTIES)
                    .message("maximum size: [%d], found: [%d]", schema.getMaxProperties(), actualSize)
                    .buildOptional();
        }

        return Optional.empty();
    }
}
