package io.dugnutt.jsonschema.validator;

import com.google.common.base.Joiner;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class SchemaValidator<S extends Schema> {

    public static final String TYPE_MISMATCH_ERROR_MESSAGE = "expected type: %s, found: %s";
    public static final String VALIDATION_KEYWORD_PREFIX = "validation.keyword.";

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

    public S schema() {
        return schema;
    }

    public abstract Optional<ValidationError> validate(PathAwareJsonValue subject);

    @Deprecated
    public Optional<ValidationError> validate(JsonValue value) {
        return validate(new PathAwareJsonValue(value, schema.getLocation().getJsonPath()));
    }

    // @Deprecated
    // protected ValidationError failure(String message, JsonSchemaKeyword keyword) {
    //     return new ValidationError(schema(), message, keyword, schema.getLocation().getJsonPointerFragment());
    // }

    protected ValidationError.ValidationErrorBuilder createBuilder(PathAwareJsonValue subject) {
        return ValidationError.validationBuilder()
                .violatedSchema(schema())
                .pointerToViolation(subject.getPath())
                .schemaLocation(schema().getDocumentLocalURI());
    }

    protected ValidationError.ValidationErrorBuilder buildKeywordFailure(PathAwareJsonValue subject, JsonSchemaKeyword keyword) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(subject, "subject must not be null");

        return createBuilder(subject)
                .keyword(keyword)
                .code(VALIDATION_KEYWORD_PREFIX + keyword);
    }

    protected ValidationError.ValidationErrorBuilder buildTypeMismatchError(PathAwareJsonValue subject, Collection<JsonSchemaType> expectedTypes) {
        checkNotNull(expectedTypes, "expectedType must not be null");
        checkNotNull(subject, "subject must not be null");

        String commaSeparatedTypes = Joiner.on(",").join(expectedTypes);
        return createBuilder(subject)
                .keyword(JsonSchemaKeyword.TYPE)
                .message("expected one of the following types: %s, found: %s", commaSeparatedTypes, subject.getJsonSchemaType())
                .code("validation.typeMismatch")
                .model(expectedTypes)
                .model(subject.getJsonSchemaType());
    }

    protected ValidationError.ValidationErrorBuilder buildTypeMismatchError(PathAwareJsonValue subject, JsonSchemaType expectedType) {
        checkNotNull(expectedType, "expectedType must not be null");
        checkNotNull(subject, "subject must not be null");

        return createBuilder(subject)
                .keyword(JsonSchemaKeyword.TYPE)
                .message(TYPE_MISMATCH_ERROR_MESSAGE, expectedType, subject.getJsonSchemaType())
                .code("validation.typeMismatch")
                .model(expectedType)
                .model(subject.getJsonSchemaType());
    }

    protected Optional<ValidationError> verifyType(PathAwareJsonValue value, JsonSchemaType expectedType, boolean required) {
        checkNotNull(value, "value must not be null");
        checkNotNull(expectedType, "expectedType must not be null");
        JsonSchemaType jsonSchemaType = value.getJsonSchemaType();
        if (!required || jsonSchemaType == expectedType) {
            return Optional.empty();
        } else {
            return buildTypeMismatchError(value, expectedType).buildOptional();
        }
    }
}
