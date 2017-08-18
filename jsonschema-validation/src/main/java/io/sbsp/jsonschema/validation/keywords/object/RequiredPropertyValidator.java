package io.sbsp.jsonschema.validation.keywords.object;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.StringSetKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import java.util.Set;

public class RequiredPropertyValidator extends KeywordValidator<StringSetKeyword> {

    private final Set<String> requiredProperties;

    public RequiredPropertyValidator(StringSetKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.REQUIRED, schema);
        this.requiredProperties = ImmutableSet.copyOf(keyword.getStringSet());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        for (String requiredProp : requiredProperties) {
            if (!subject.containsKey(requiredProp)) {
                report.addError(buildKeywordFailure(subject)
                        .message("required key [%s] not found", requiredProp)
                        .build());

            }
        }
        return report.isValid();
    }
}
