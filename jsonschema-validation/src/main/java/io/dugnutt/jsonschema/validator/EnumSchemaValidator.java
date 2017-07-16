package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.EnumSchema;
import io.dugnutt.jsonschema.six.ObjectComparator;

import javax.json.JsonValue;
import java.util.Optional;

import static java.lang.String.format;

public class EnumSchemaValidator extends SchemaValidator<EnumSchema> {

    public EnumSchemaValidator(EnumSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue toBeValidated) {
        boolean foundMatch = schema.getPossibleValues()
                .stream()
                .anyMatch(val -> ObjectComparator.lexicalEquivalent(val, toBeValidated));
        if (!foundMatch) {
            return Optional.of(failure(format("%s is not a valid enum value", toBeValidated), "enum"));
        }
        return Optional.empty();
    }
}
