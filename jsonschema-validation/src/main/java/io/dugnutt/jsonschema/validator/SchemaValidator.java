package io.dugnutt.jsonschema.validator;

import com.google.common.base.Preconditions;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;

import javax.json.JsonValue;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class SchemaValidator<S extends Schema> {

    protected final S schema;
    protected final SchemaValidatorFactory factory;

    public SchemaValidator(S schema) {
        this.schema = checkNotNull(schema);
        this.factory = SchemaValidatorFactory.DEFAULT_VALIDATOR;
    }

    public SchemaValidator(S schema, SchemaValidatorFactory factory) {
        this.schema = checkNotNull(schema);
        this.factory = checkNotNull(factory);
    }

    public static ValidationError failure(Schema validating, JsonSchemaType expectedType, JsonSchemaType foundType) {
        String message = String.format("expected type: %s, found: %s", expectedType, foundType);
        return new ValidationError(validating, message, JsonSchemaKeyword.TYPE, validating.getDocumentLocalURI());
    }

    public ValidationError failure(String message, JsonSchemaKeyword keyword, List<ValidationError> causes) {
        Preconditions.checkNotNull(keyword, "keyword must not be null");
        Preconditions.checkNotNull(message, "message must not be null");
        return new ValidationError(this.schema(), new StringBuilder("#"), message, causes, keyword,
                this.schema().getDocumentLocalURI());
    }

    public S schema() {
        return schema;
    }

    public abstract Optional<ValidationError> validate(JsonValue toBeValidated);

    protected ValidationError failure(String message, JsonSchemaKeyword keyword) {
        return new ValidationError(schema(), message, keyword, schema().getDocumentLocalURI());
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
}
