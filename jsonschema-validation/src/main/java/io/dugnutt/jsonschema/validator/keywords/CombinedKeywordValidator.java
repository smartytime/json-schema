package io.dugnutt.jsonschema.validator.keywords;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationError;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static io.dugnutt.jsonschema.loader.SchemaLoadingContext.COMBINED_SCHEMA_KEYWORDS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

@Builder
public class CombinedKeywordValidator implements SchemaValidator {
    @Singular
    @NonNull
    private final List<SchemaValidator> subschemaValidators;

    @NonNull
    private final JsonSchemaKeyword combinedKeyword;

    @NonNull
    private final Schema parentSchema;

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport parentReport) {
        checkArgument(COMBINED_SCHEMA_KEYWORDS.contains(combinedKeyword), "Should contain this item");
        ValidationReport report = new ValidationReport();
        for (SchemaValidator validator : subschemaValidators) {
            validator.validate(subject, report);
        }

        List<ValidationError> failures = report.getErrors();

        int matchingCount = subschemaValidators.size() - failures.size();
        int subschemaCount = subschemaValidators.size();

        switch (combinedKeyword) {
            case ALL_OF:
                if (matchingCount < subschemaCount) {
                    return parentReport.addError(buildKeywordFailure(subject, parentSchema, ALL_OF)
                            .message("only %d subschema matches out of %d", matchingCount, subschemaCount)
                            .causingExceptions(failures)
                            .build());
                }
                break;
            case ONE_OF:
                if (matchingCount != 1) {
                    return parentReport.addError(buildKeywordFailure(subject, parentSchema, ONE_OF)
                            .message("%d subschemas matched instead of one", matchingCount)
                            .causingExceptions(failures)
                            .build());
                }
                break;
        }
        return true;
    }
}
