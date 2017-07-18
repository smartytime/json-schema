package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.NotSchema;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Optional;

public class NotSchemaValidator extends SchemaValidator<NotSchema> {

    public NotSchemaValidator(NotSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue toBeValidated) {
        Schema mustNotMatch = schema.getMustNotMatch();
        Optional<ValidationError> validated = SchemaValidatorFactory.createValidatorForSchema(mustNotMatch)
                .validate(toBeValidated);
        if (!validated.isPresent()) {
            return Optional.of(failure("subject must not be valid against schema " + mustNotMatch, JsonSchemaKeyword.NOT));
        }
        return Optional.empty();
    }
}
