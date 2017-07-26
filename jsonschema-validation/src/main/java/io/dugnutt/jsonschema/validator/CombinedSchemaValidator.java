package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static io.dugnutt.jsonschema.loader.SchemaLoadingContext.COMBINED_SCHEMA_KEYWORDS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class CombinedSchemaValidator {

    private CombinedSchemaValidator() {
    }

    public static CombinedSchemaValidator combinedSchemaValidator() {
        return new CombinedSchemaValidator();
    }

    public Optional<ValidationError> validate(PathAwareJsonValue subject, Schema parentSchema, SchemaValidatorFactory factory,
                                              List<Schema> subschemas, JsonSchemaKeyword combinedType) {
        checkArgument(COMBINED_SCHEMA_KEYWORDS.contains(combinedType), "Should contain this item");
        List<ValidationError> failures = subschemas.stream()
                .map(schema ->
                        factory.createValidator(schema)
                                .validate(subject)
                                .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        int matchingCount = subschemas.size() - failures.size();
        int subschemaCount = subschemas.size();

        switch (combinedType) {
            case ANY_OF:
                if (matchingCount == 0) {
                    return buildKeywordFailure(subject, parentSchema, ANY_OF)
                            .message("no subschema matched out of the total %d subschemas", subschemaCount)
                            .causingExceptions(failures)
                            .buildOptional();
                }
                break;
            case ALL_OF:
                if (matchingCount < subschemaCount) {
                    return buildKeywordFailure(subject, parentSchema, ALL_OF)
                            .message("only %d subschema matches out of %d", matchingCount, subschemaCount)
                            .causingExceptions(failures)
                            .buildOptional();
                }
                break;
            case ONE_OF:
                if (matchingCount != 1) {
                    return buildKeywordFailure(subject, parentSchema, ONE_OF)
                            .message("%d subschemas matched instead of one", matchingCount)
                            .causingExceptions(failures)
                            .buildOptional();
                }
                break;
        }
        return Optional.empty();
    }
}
