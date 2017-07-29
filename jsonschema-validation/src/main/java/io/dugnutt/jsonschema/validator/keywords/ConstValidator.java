package io.dugnutt.jsonschema.validator.keywords;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;

import javax.json.JsonValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ConstValidator extends KeywordValidator {

    private final JsonValue constValue;

    @Builder
    public ConstValidator(Schema parentSchema, JsonValue constValue) {
        super(JsonSchemaKeyword.CONST, parentSchema);
        this.constValue = checkNotNull(constValue);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        if (!constValue.equals(subject)) {
            report.addError(buildKeywordFailure(subject, schema, CONST)
                    .message("%s does not match the const value", subject)
                    .build());
        }
        return report.isValid();
    }
}
