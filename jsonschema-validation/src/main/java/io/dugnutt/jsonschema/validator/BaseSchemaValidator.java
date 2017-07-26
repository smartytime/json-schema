package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.ObjectComparator;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;
import static io.dugnutt.jsonschema.validator.CombinedSchemaValidator.combinedSchemaValidator;

public class BaseSchemaValidator implements PartialSchemaValidator {

    private static final BaseSchemaValidator INSTANCE = new BaseSchemaValidator();

    private BaseSchemaValidator() {
    }

    public Optional<ValidationError> validate(PathAwareJsonValue subject, JsonSchema schema, SchemaValidatorFactory factory) {

        // Do all the core validations
        List<ValidationError> allErrors = new ArrayList<>();

        validateEnum(subject, schema).ifPresent(allErrors::add);
        validateConst(subject, schema).ifPresent(allErrors::add);
        validateNot(subject, schema, factory).ifPresent(allErrors::add);
        validateCombinedSchema(schema, schema.getAllOfSchemas(), factory, subject, ALL_OF).ifPresent(allErrors::add);
        validateCombinedSchema(schema, schema.getAnyOfSchemas(), factory, subject, ANY_OF).ifPresent(allErrors::add);
        validateCombinedSchema(schema, schema.getOneOfSchemas(), factory, subject, ONE_OF).ifPresent(allErrors::add);

        return ValidationError.collectErrors(schema, subject.getPath(), allErrors);
    }


    public Optional<ValidationError> validateConst(PathAwareJsonValue toBeValidated, JsonSchema schema) {
        return schema.getConstValue()
                .map(constValue -> {
                    if (!constValue.equals(toBeValidated)) {
                        return ValidationErrorHelper.buildKeywordFailure(toBeValidated, schema, CONST)
                                .message("%s does not match the const value", toBeValidated)
                                .build();
                    } else {
                        return null;
                    }
                });
    }

    public Optional<ValidationError> validateEnum(PathAwareJsonValue toBeValidated, JsonSchema schema) {
        return schema.getEnumValues()
                .map(enumValues -> {
                    boolean foundMatch = enumValues.stream()
                            .anyMatch(val -> ObjectComparator.lexicalEquivalent(val, toBeValidated.getWrapped()));
                    if (!foundMatch) {
                        return ValidationErrorHelper.buildKeywordFailure(toBeValidated, schema, ENUM)
                                .message("%s does not match the enum values", toBeValidated)
                                .build();
                    }
                    return null;
                });
    }

    public Optional<ValidationError> validateNot(PathAwareJsonValue toBeValidated, JsonSchema schema, SchemaValidatorFactory factory) {
        return schema.getNotSchema()
                .map(notSchema -> {
                    Optional<ValidationError> validated = factory.createValidator(notSchema)
                            .validate(toBeValidated);
                    if (!validated.isPresent()) {
                        return ValidationErrorHelper.buildKeywordFailure(toBeValidated, schema, NOT)
                                .message("subject must not be valid against schema", notSchema)
                                .build();
                    }
                    return null;
                });
    }

    private Optional<ValidationError> validateCombinedSchema(JsonSchema parent, List<JsonSchema> subschemas, SchemaValidatorFactory factory,
                                                             PathAwareJsonValue subject, JsonSchemaKeyword combinedType) {
        if (subschemas != null && subschemas.size() > 0) {
            return combinedSchemaValidator().validate(subject, parent, factory, subschemas, combinedType);
        }
        return Optional.empty();
    }

    public static BaseSchemaValidator baseSchemaValidator() {
        return INSTANCE;
    }
}
