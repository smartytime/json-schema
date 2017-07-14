package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.NotSchema;
import org.everit.jsonschema.api.Schema;

import javax.json.JsonValue;
import java.util.Optional;

public class NotSchemaValidator extends SchemaValidator<NotSchema> {

    public NotSchemaValidator(NotSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue toBeValidated) {
        Schema mustNotMatch = schema.getMustNotMatch();
        Optional<ValidationError> validated = SchemaValidatorFactory.findValidator(mustNotMatch)
                .validate(toBeValidated);
        if (!validated.isPresent()) {
            return Optional.of(failure("subject must not be valid against schema " + mustNotMatch, "not"));
        }
        return Optional.empty();
    }
}
