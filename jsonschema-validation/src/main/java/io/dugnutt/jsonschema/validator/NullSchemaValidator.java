package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.NullSchema;

import javax.json.JsonValue;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class NullSchemaValidator extends SchemaValidator<NullSchema> {
    public NullSchemaValidator(NullSchema schema) {
        super(schema);
    }

    public NullSchemaValidator(NullSchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue toBeValidated) {
        checkNotNull(toBeValidated, "toBeValidated must not be null");
        if (toBeValidated.getValueType() != JsonValue.ValueType.NULL) {
            return buildKeywordFailure(toBeValidated, JsonSchemaKeyword.TYPE)
                    .message("expected: null, found: %s", toBeValidated.getValueType())
                    .buildOptional();
        }
        return Optional.empty();
    }
}
