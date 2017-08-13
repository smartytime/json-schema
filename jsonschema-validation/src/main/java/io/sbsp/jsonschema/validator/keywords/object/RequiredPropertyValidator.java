package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.StringSetKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.REQUIRED;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class RequiredPropertyValidator extends KeywordValidator<StringSetKeyword> {

    private final Set<String> requiredProperties;

    public RequiredPropertyValidator(StringSetKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.required, schema);
        this.requiredProperties = ImmutableSet.copyOf(keyword.getStringSet());
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
