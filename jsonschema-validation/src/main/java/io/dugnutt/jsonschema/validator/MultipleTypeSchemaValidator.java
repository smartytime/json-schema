package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.MultipleTypeSchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import java.util.Optional;

public class MultipleTypeSchemaValidator extends SchemaValidator<MultipleTypeSchema> {

    public MultipleTypeSchemaValidator(MultipleTypeSchema schema) {
        super(schema);
    }

    public MultipleTypeSchemaValidator(MultipleTypeSchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject) {
        JsonSchemaType inputType = subject.getJsonSchemaType();
        return this.schema.getSchemaForType(inputType)
                .map(schema -> factory.createValidator(schema).validate(subject))
                .orElseGet(()->{
                    if (schema.isRequireOne()) {
                        return buildTypeMismatchError(subject, schema.possibleSchemaTypes()).buildOptional();
                    }
                    return Optional.empty();
                });
    }
}
