package io.dugnutt.jsonschema.validator;

import com.google.common.base.Joiner;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class ValidationErrorHelper {

    public static final String TYPE_MISMATCH_ERROR_MESSAGE = "expected type: %s, found: %s";
    public static final String VALIDATION_KEYWORD_PREFIX = "validation.keyword.";

    public static ValidationError.ValidationErrorBuilder buildKeywordFailure(PathAwareJsonValue subject, Schema schema, JsonSchemaKeyword keyword) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(subject, "subject must not be null");

        return createBuilder(subject, schema)
                .keyword(keyword)
                .code(VALIDATION_KEYWORD_PREFIX + keyword);
    }

    public static ValidationError.ValidationErrorBuilder buildTypeMismatchError(PathAwareJsonValue subject, Schema schema, Collection<JsonSchemaType> expectedTypes) {
        checkNotNull(expectedTypes, "expectedType must not be null");
        checkNotNull(subject, "subject must not be null");

        String commaSeparatedTypes = Joiner.on(",").join(expectedTypes);
        return createBuilder(subject, schema)
                .keyword(JsonSchemaKeyword.TYPE)
                .message("expected one of the following keywords: %s, found: %s", commaSeparatedTypes, subject.getJsonSchemaType())
                .code("validation.typeMismatch")
                .model(expectedTypes)
                .model(subject.getJsonSchemaType());
    }

    public static ValidationError.ValidationErrorBuilder buildTypeMismatchError(PathAwareJsonValue subject, Schema schema, JsonSchemaType expectedType) {
        checkNotNull(expectedType, "expectedType must not be null");
        checkNotNull(subject, "subject must not be null");

        return createBuilder(subject, schema)
                .keyword(JsonSchemaKeyword.TYPE)
                .message(TYPE_MISMATCH_ERROR_MESSAGE, expectedType, subject.getJsonSchemaType())
                .code("validation.typeMismatch")
                .model(expectedType)
                .model(subject.getJsonSchemaType());
    }

    public static ValidationError.ValidationErrorBuilder createBuilder(PathAwareJsonValue subject, Schema schema) {
        return ValidationError.validationBuilder()
                .violatedSchema(schema)
                .pointerToViolation(subject.getPath())
                .schemaLocation(schema.getLocation().getJsonPointerFragment());
    }

    public static Optional<ValidationError> verifyType(PathAwareJsonValue value, Schema schema, JsonSchemaType expectedType, boolean required) {
        checkNotNull(value, "value must not be null");
        checkNotNull(expectedType, "expectedType must not be null");
        JsonSchemaType jsonSchemaType = value.getJsonSchemaType();
        if (!required || jsonSchemaType == expectedType) {
            return Optional.empty();
        } else {
            return buildTypeMismatchError(value, schema, expectedType).buildOptional();
        }
    }
}
