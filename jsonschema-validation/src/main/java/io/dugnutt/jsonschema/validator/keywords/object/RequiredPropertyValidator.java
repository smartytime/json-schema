package io.dugnutt.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableSet;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.Set;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.*;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class RequiredPropertyValidator extends KeywordValidator {

    @NonNull
    private final Set<String> requiredProperties;

    @Builder
    public RequiredPropertyValidator(Schema schema, Set<String> requiredProperties) {
        super(REQUIRED, schema);
        this.requiredProperties = ImmutableSet.copyOf(requiredProperties);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        for (String requiredProp : requiredProperties) {
            if (!subject.containsKey(requiredProp)) {
                report.addError(buildKeywordFailure(subject, schema, REQUIRED)
                        .message("required key [%s] not found", requiredProp)
                        .build());

            }
        }
        return report.isValid();
    }
}
