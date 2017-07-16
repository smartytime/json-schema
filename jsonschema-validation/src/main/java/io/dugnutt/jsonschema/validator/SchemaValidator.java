package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;

import javax.json.JsonValue;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class SchemaValidator<S extends Schema> {

    protected final S schema;

    public SchemaValidator(S schema) {
        this.schema = checkNotNull(schema);
    }

    public S schema() {
        return schema;
    }

    public abstract Optional<ValidationError> validate(JsonValue toBeValidated);

    protected ValidationError failure(String message, String keyword) {
        return new ValidationError(schema(), message, keyword, schema().getSchemaLocation());
    }

    protected ValidationError failure(JsonSchemaType expectedType, JsonValue value) {
        checkNotNull(value, "value must not be null");
        return failure(expectedType, JsonUtils.schemaTypeFor(value));
    }

    protected Optional<ValidationError> verifyType(JsonValue value, JsonSchemaType expectedType, boolean required) {
        checkNotNull(value, "value must not be null");
        checkNotNull(expectedType, "expectedType must not be null");
        JsonSchemaType jsonSchemaType = JsonUtils.schemaTypeFor(value);
        if (!required || jsonSchemaType == expectedType) {
            return Optional.empty();
        } else {
            return Optional.of(failure(expectedType, jsonSchemaType));
        }
    }

    protected ValidationError failure(JsonSchemaType expectedType, JsonSchemaType foundType) {
        return failure(schema(), expectedType, foundType);
    }

    public static ValidationError failure(Schema validating, JsonSchemaType expectedType, JsonSchemaType foundType) {
        String message = String.format("expected type: %s, found: %s", expectedType, foundType);
        return new ValidationError(validating, message, "typeMismatch", validating.getSchemaLocation());
    }


}
