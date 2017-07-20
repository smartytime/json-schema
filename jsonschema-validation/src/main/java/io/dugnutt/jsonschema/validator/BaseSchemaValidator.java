package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.ObjectComparator;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;

public class BaseSchemaValidator<X extends Schema> extends SchemaValidator<X> {

    private final SchemaValidator<X> wrapped;

    public BaseSchemaValidator(X schema, SchemaValidator<X> wrapped) {
        super(schema);
        this.wrapped = wrapped;
    }

    public BaseSchemaValidator(X schema, SchemaValidatorFactory factory, SchemaValidator<X> wrapped) {
        super(schema, factory);
        this.wrapped = wrapped;
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue toBeValidated) {

        // Do all the core validations
        List<ValidationError> allErrors = new ArrayList<>();

        validateEnum(toBeValidated).ifPresent(allErrors::add);
        validateConst(toBeValidated).ifPresent(allErrors::add);
        validateNot(toBeValidated).ifPresent(allErrors::add);
        validateCombinedSchema(schema.getAllOfSchema(), toBeValidated).ifPresent(allErrors::add);
        validateCombinedSchema(schema.getAllOfSchema(), toBeValidated).ifPresent(allErrors::add);
        validateCombinedSchema(schema.getAllOfSchema(), toBeValidated).ifPresent(allErrors::add);

        //Also perform whatever specific type validation we need to
        wrapped.validate(toBeValidated).ifPresent(allErrors::add);

        return ValidationError.collectErrors(schema, toBeValidated.getPath(), allErrors);
    }

    public Optional<ValidationError> validateConst(PathAwareJsonValue toBeValidated) {
        JsonValue constValue = schema.getConstValue();
        if (constValue != null && !constValue.equals(toBeValidated)) {
            return buildKeywordFailure(toBeValidated, CONST)
                            .message("%s does not match the const value", toBeValidated)
                            .buildOptional();

        }
        return Optional.empty();
    }

    public Optional<ValidationError> validateEnum(PathAwareJsonValue toBeValidated) {
        if (schema.getEnumValues() != null) {
            boolean foundMatch = schema.getEnumValues()
                    .stream()
                    .anyMatch(val -> ObjectComparator.lexicalEquivalent(val, toBeValidated.getWrapped()));
            if (!foundMatch) {
                return buildKeywordFailure(toBeValidated, ENUM)
                        .message("%s does not match the enum values", toBeValidated)
                        .buildOptional();
            }
        }
        return Optional.empty();
    }

    public Optional<ValidationError> validateNot(PathAwareJsonValue toBeValidated) {
        Schema mustNotMatch = schema.getNotSchema();
        if (mustNotMatch != null) {
            Optional<ValidationError> validated = factory.createValidator(mustNotMatch)
                    .validate(toBeValidated);
            if (!validated.isPresent()) {
                return buildKeywordFailure(toBeValidated, NOT)
                        .message("subject must not be valid against schema", mustNotMatch)
                        .buildOptional();
            }
        }

        return Optional.empty();
    }

    private Optional<ValidationError> validateCombinedSchema(CombinedSchema schema, PathAwareJsonValue toBeValidated) {
        if (schema != null) {
            return factory.createValidator(schema).validate(toBeValidated);
        }
        return Optional.empty();
    }
}
