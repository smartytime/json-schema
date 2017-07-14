package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.ObjectSchema;
import org.everit.jsonschema.api.Schema;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

public class ObjectSchemaValidator extends SchemaValidator<ObjectSchema> {

    public ObjectSchemaValidator(ObjectSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue subject) {

        if (subject.getValueType() != JsonValue.ValueType.OBJECT && schema.requiresObject()) {
            return Optional.of(failure(JsonValue.ValueType.OBJECT, subject.getValueType()));
        } else if (subject.getValueType() == JsonValue.ValueType.OBJECT) {
            List<ValidationError> failures = new ArrayList<>();
            JsonObject objSubject = (JsonObject) subject;
            failures.addAll(testProperties(objSubject));
            failures.addAll(testRequiredProperties(objSubject));
            failures.addAll(testAdditionalProperties(objSubject));
            failures.addAll(testPropertyDependencies(objSubject));
            failures.addAll(testSchemaDependencies(objSubject));
            failures.addAll(testPatternProperties(objSubject));

            testSize(objSubject).ifPresent(failures::add);
            return ValidationError.collectErrors(schema, failures);
        }
        return Optional.empty();
    }

    private List<ValidationError> testAdditionalProperties(final JsonObject subject) {
        if (!schema.permitsAdditionalProperties()) {
            return schema.getAdditionalProperties(subject)
                    .map(unneeded -> String.format("extraneous key [%s] is not permitted", unneeded))
                    .map(msg -> new ValidationError(schema, msg, "additionalProperties"))
                    .collect(Collectors.toList());
        } else if (schema.getSchemaOfAdditionalProperties() != null) {
            List<String> additionalPropNames = schema.getAdditionalProperties(subject)
                    .collect(Collectors.toList());
            List<ValidationError> allErrors = new ArrayList<>();
            Schema addtlSchema = schema.getSchemaOfAdditionalProperties();
            additionalPropNames.stream()
                    .map(getValidationErrors(addtlSchema, subject))
                    .forEach(addErrorIfExists(allErrors));
            return allErrors;
        }
        return emptyList();
    }

    private List<ValidationError> testPatternProperties(final JsonObject subject) {
        Set<String> subjectProperties = subject.keySet();
        if (subjectProperties.isEmpty()) {
            return emptyList();
        }
        List<ValidationError> allErrors = new ArrayList<>();
        schema.getPatternProperties().forEach((pattern, schema) -> {
            subjectProperties.stream()
                    .filter(regexMatches(pattern))
                    .map(getValidationErrors(schema, subject))
                    .forEach(addErrorIfExists(allErrors));
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

    private Function<String, Optional<ValidationError>> getValidationErrors(Schema validateAgainst, JsonObject sourceObject) {
        return propertyName -> SchemaValidatorFactory.findValidator(validateAgainst)
                .validate(sourceObject.getValue(propertyName))
                .map(prependPropertyToError(propertyName));
    }

    private Function<ValidationError, ValidationError> prependPropertyToError(String property) {
        return error -> error.prepend(property);
    }

    private Function<ValidationError, ValidationError> prependPropertyToError(String property, Schema schema) {
        return error -> error.prepend(property, schema);
    }

    private List<ValidationError> testProperties(final JsonObject subject) {
        Map<String, Schema> propertySchemas = schema.getPropertySchemas();
        if (schema.getPropertySchemas() != null) {
            List<ValidationError> errors = new ArrayList<>();
            schema.getPropertySchemas().forEach((propertyName, schema) -> {
                if (subject.containsKey(propertyName)) {
                    SchemaValidatorFactory.findValidator(schema)
                            .validate(subject.getValue(propertyName))
                            .map(prependPropertyToError(propertyName))
                            .ifPresent(errors::add);
                }
            });
            return errors;
        }
        return emptyList();
    }

    private List<ValidationError> testPropertyDependencies(final JsonObject subject) {
        return schema.getPropertyDependencies().keySet().stream()
                .filter(subject::containsKey)
                .flatMap(ifPresent -> schema.getPropertyDependencies().get(ifPresent).stream())
                .filter(mustBePresent -> !subject.containsKey(mustBePresent))
                .map(missingKey -> String.format("property [%s] is required", missingKey))
                .map(excMessage -> failure(excMessage, "dependencies"))
                .collect(Collectors.toList());
    }

    private List<ValidationError> testRequiredProperties(final JsonObject subject) {
        return schema.getRequiredProperties().stream()
                .filter(key -> !subject.containsKey(key))
                .map(missingKey -> String.format("required key [%s] not found", missingKey))
                .map(excMessage -> failure(excMessage, "required"))
                .collect(Collectors.toList());
    }

    private List<ValidationError> testSchemaDependencies(final JsonObject subject) {
        List<ValidationError> rval = new ArrayList<>();
        schema.getSchemaDependencies().forEach((propName, schema) -> {
            if (subject.containsKey(propName)) {
                SchemaValidatorFactory.findValidator(schema)
                        .validate(subject)
                        .ifPresent(rval::add);
            }
        });
        return rval;
    }

    private Optional<ValidationError> testSize(final JsonObject subject) {
        int actualSize = subject.size();
        if (schema.getMinProperties() != null && actualSize < schema.getMinProperties()) {
            return Optional.of(failure(String.format("minimum size: [%d], found: [%d]", schema.getMinProperties(), actualSize),
                    "minProperties"));
        }
        if (schema.getMaxProperties() != null && actualSize > schema.getMaxProperties()) {
            return Optional.of(failure(String.format("maximum size: [%d], found: [%d]", schema.getMaxProperties(), actualSize),
                    "maxProperties"));
        }

        return Optional.empty();
    }
}
