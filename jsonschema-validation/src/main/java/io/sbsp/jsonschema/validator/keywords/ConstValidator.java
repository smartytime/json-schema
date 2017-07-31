package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;

import javax.json.JsonValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.CONST;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ConstValidator extends KeywordValidator {

    private final JsonValue constValue;

    @Builder
    public ConstValidator(Schema parentSchema, JsonValue constValue) {
        super(JsonSchemaKeyword.CONST, parentSchema);
        this.constValue = checkNotNull(constValue);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        if (!constValue.equals(subject.getWrapped())) {
            report.addError(buildKeywordFailure(subject, schema, CONST)
                    .message("%s does not match the const value", subject)
                    .build());
        }
        return report.isValid();
    }
}
