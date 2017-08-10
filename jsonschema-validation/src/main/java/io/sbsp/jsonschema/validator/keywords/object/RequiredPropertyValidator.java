package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.StringSetKeyword;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.*;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class RequiredPropertyValidator extends KeywordValidator<StringSetKeyword> {

    @NonNull
    private final Set<String> requiredProperties;

    @Builder
    public RequiredPropertyValidator(Schema schema, Set<String> requiredProperties) {
        super(SchemaKeyword.required, schema);
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
