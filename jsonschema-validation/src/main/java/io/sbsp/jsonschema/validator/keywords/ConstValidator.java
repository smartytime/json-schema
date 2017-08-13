package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.JsonValueKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONST;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ConstValidator extends KeywordValidator<JsonValueKeyword> {

    private final JsonValue constValue;

    @Builder
    public ConstValidator(JsonValueKeyword keyword, Schema parentSchema, SchemaValidatorFactory factory) {
        super(Keywords.$const, parentSchema);
        this.constValue = keyword.getKeywordValue();
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
