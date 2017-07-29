package io.dugnutt.jsonschema.validator;

import com.google.common.base.Joiner;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class ValidationErrorHelper {

    public static final String TYPE_MISMATCH_ERROR_MESSAGE = "expected type: %s, found: %s";
    public static final String VALIDATION_KEYWORD_PREFIX = "validation.keyword.";

    public static ValidationError.ValidationErrorBuilder buildKeywordFailure(JsonValueWithLocation subject, Schema schema, JsonSchemaKeyword keyword) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(subject, "subject must not be null");

        return createBuilder(subject, schema)
                .keyword(keyword)
                .code(VALIDATION_KEYWORD_PREFIX + keyword);
    }

    public static ValidationError.ValidationErrorBuilder buildTypeMismatchError(JsonValueWithLocation subject, Schema schema, Collection<JsonSchemaType> expectedTypes) {
        checkNotNull(expectedTypes, "expectedType must not be null");
        checkNotNull(subject, "subject must not be null");

        if (expectedTypes.size() == 1) {
            return buildTypeMismatchError(subject, schema, expectedTypes.iterator().next());
        }

        String commaSeparatedTypes = Joiner.on(",").join(expectedTypes);
        return createBuilder(subject, schema)
                .keyword(JsonSchemaKeyword.TYPE)
                .message("expected one of the following types: %s, found: %s", commaSeparatedTypes, subject.getJsonSchemaType())
                .code("validation.typeMismatch");
    }

    public static ValidationError.ValidationErrorBuilder buildTypeMismatchError(JsonValueWithLocation subject, Schema schema, JsonSchemaType expectedType) {
        checkNotNull(expectedType, "expectedType must not be null");
        checkNotNull(subject, "subject must not be null");

        return createBuilder(subject, schema)
                .keyword(JsonSchemaKeyword.TYPE)
                .message(TYPE_MISMATCH_ERROR_MESSAGE, expectedType, subject.getJsonSchemaType())
                .code("validation.typeMismatch");

    }

    public static ValidationError.ValidationErrorBuilder createBuilder(JsonValueWithLocation subject, Schema schema) {
        return ValidationError.validationBuilder()
                .violatedSchema(schema)
                .pointerToViolation(subject.getPath())
                .schemaLocation(schema.getLocation().getJsonPointerFragment());
    }
}
