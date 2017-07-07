package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.EnumSchema;
import org.everit.jsonschema.api.ObjectComparator;
import org.everit.json.JsonElement;

import java.util.Optional;

import static java.lang.String.format;

public class EnumSchemaValidator extends SchemaValidator<EnumSchema> {

    public EnumSchemaValidator(EnumSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> toBeValidated) {
        Object effectiveSubject = toBeValidated.raw();
        boolean foundMatch = schema.getPossibleValues()
                .stream()
                .anyMatch(val -> ObjectComparator.deepEquals(val, effectiveSubject));
        if (!foundMatch) {
            return Optional.of(failure(format("%s is not a valid enum value", toBeValidated), "enum"));
        }
        return Optional.empty();
    }

}
