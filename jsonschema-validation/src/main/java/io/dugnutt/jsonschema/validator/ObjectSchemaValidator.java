package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.Schema;
import lombok.experimental.var;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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

    @Override
    public Optional<ValidationError> validate(JsonValue subject) {

        //todo:ericm Add propertyNames schema
        var wrongType = verifyType(subject, JsonSchemaType.OBJECT, schema.isRequiresObject());
        if (wrongType.isPresent()) {
            return wrongType;
        }

        if (subject.getValueType() == JsonValue.ValueType.OBJECT) {
            List<ValidationError> failures = new ArrayList<>();
            JsonObject objSubject = (JsonObject) subject;

            // These are nested by nature
            failures.addAll(testProperties(objSubject));

            failures.addAll(testRequiredProperties(objSubject));

            failures.addAll(testPropertyDependencies(objSubject));
            failures.addAll(testSchemaDependencies(objSubject));
            failures.addAll(testPatternProperties(objSubject));

            testAdditionalProperties(objSubject).ifPresent(failures::add);
            testSize(objSubject).ifPresent(failures::add);
            return ValidationError.collectErrors(schema, failures);
        }
        return Optional.empty();
    }

    private Optional<ValidationError> testAdditionalProperties(final JsonObject subject) {
        final SchemaValidatorFactory factory = context.getFactory();
        final List<ValidationError> additionalPropertyErrors = new ArrayList<>();
        schema.getSchemaOfAdditionalProperties()
                .map(factory::createValidator)
                .ifPresent(extraPropertyValidator ->
                        schema.getAdditionalProperties(subject)
                                .forEach(propertyName -> {
                                    JsonValue propertyValue = subject.get(propertyName);
                                    Optional<ValidationError> error = extraPropertyValidator.validate(propertyValue)
                                            .map(err->err.prepend(propertyName));
                                    error.ifPresent(additionalPropertyErrors::add);
                                })

                );

        if (additionalPropertyErrors.size() > 0) {
            return Optional.of(failure("Additional properties were invalid", ADDITIONAL_PROPERTIES, additionalPropertyErrors));
        }
        return Optional.empty();

    }

    private List<ValidationError> testPatternProperties(final JsonObject subject) {
        Set<String> subjectProperties = subject.keySet();
        if (subjectProperties.isEmpty()) {
            return emptyList();
        }
        List<ValidationError> allErrors = new ArrayList<>();
        schema.getPatternProperties().forEach((pattern, patternSchema) -> {
            subjectProperties.stream()
                    .filter(regexMatches(pattern))
                    .forEach(propertyName -> {
                        final JsonValue propertyValue = subject.get(propertyName);
                        final Optional<ValidationError> error = context.getFactory().createValidator(patternSchema)
                                .validate(propertyValue)
                                .map(err -> err.prepend(propertyName));
                        error.ifPresent(allErrors::add);
                    });
        });
        return allErrors;
    }

    private Consumer<Optional<ValidationError>> addErrorIfExists(List<ValidationError> errors) {
        return error -> error.ifPresent(errors::add);
    }

    private Predicate<String> regexMatches(Pattern regex) {
        checkNotNull(regex, "regex must not be null");
        return string -> regex.matcher(string).find();
    }

    private List<ValidationError> testProperties(final JsonObject subject) {
        final SchemaValidatorFactory factory = context.getFactory();

        final List<ValidationError> propertyErrors = new ArrayList<>();
        final List<ValidationError> propertyNameErrors = new ArrayList<>();

        final Optional<SchemaValidator<Schema>> propertyNameValidator = schema.getPropertyNameSchema().map(factory::createValidator);

        subject.forEach((propertyName, propertyValue) -> {
            // Validate against property schema if one exists
            schema.findPropertySchema(propertyName)
                    .ifPresent(propertySchema -> {
                        factory.createValidator(propertySchema)
                                .validate(propertyValue)
                                .map(err -> err.prepend(propertyName, propertySchema))
                                .ifPresent(propertyErrors::add);
                    });

            // Validate against property name schema if one exists
            propertyNameValidator.ifPresent(validator -> {
                //The validators work against json objects, not raw java objects, so we need to
                //wrap this in a JsonString here.
                final JsonString jsonValue = context.getProvider().createValue(propertyName);
                validator.validate(jsonValue)
                        .map(err -> err.prepend(propertyName))
                        .ifPresent(propertyNameErrors::add);
            });
        });

        if (propertyNameErrors.size() > 0) {
            propertyErrors.add(failure("Invalid property names", PROPERTY_NAMES, propertyNameErrors));
        }

        return propertyErrors;
    }

    private List<ValidationError> testPropertyDependencies(final JsonObject subject) {
        return schema.getPropertyDependencies().keySet().stream()
                .filter(subject::containsKey)
                .flatMap(this::getDependenciesForProperty)
                .filter(mustBePresent -> !subject.containsKey(mustBePresent))
                .map(missingKey -> String.format("property [%s] is required", missingKey))
                .map(excMessage -> failure(excMessage, DEPENDENCIES))
                .collect(Collectors.toList());
    }

    private Stream<String> getDependenciesForProperty(String forProperty) {
        return schema.getPropertyDependencies().get(forProperty).stream();
    }

    private List<ValidationError> testRequiredProperties(final JsonObject subject) {
        return schema.getRequiredProperties().stream()
                .filter(key -> !subject.containsKey(key))
                .map(missingKey -> String.format("required key [%s] not found", missingKey))
                .map(excMessage -> failure(excMessage, REQUIRED))
                .collect(Collectors.toList());
    }

    private List<ValidationError> testSchemaDependencies(final JsonObject subject) {
        List<ValidationError> errors = new ArrayList<>();
        schema.getSchemaDependencies().forEach((propName, schema) -> {
            if (subject.containsKey(propName)) {
                context.getFactory().createValidator(schema)
                        .validate(subject)
                        .ifPresent(errors::add);
            }
        });
        return errors;
    }

    private Optional<ValidationError> testSize(final JsonObject subject) {
        int actualSize = subject.size();
        if (schema.getMinProperties() != null && actualSize < schema.getMinProperties()) {
            return Optional.of(failure(String.format("minimum size: [%d], found: [%d]", schema.getMinProperties(), actualSize),
                    MIN_PROPERTIES));
        }
        if (schema.getMaxProperties() != null && actualSize > schema.getMaxProperties()) {
            return Optional.of(failure(String.format("maximum size: [%d], found: [%d]", schema.getMaxProperties(), actualSize),
                    MAX_PROPERTIES));
        }

        return Optional.empty();
    }
}
