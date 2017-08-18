package io.sbsp.jsonschema.validation.keywords;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.JsonValueKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import lombok.Builder;

import javax.json.JsonValue;

public class ConstValidator extends KeywordValidator<JsonValueKeyword> {

    private final JsonValue constValue;

    @Builder
    public ConstValidator(JsonValueKeyword keyword, Schema parentSchema, SchemaValidatorFactory factory) {
        super(Keywords.CONST, parentSchema);
        this.constValue = keyword.getKeywordValue();
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        if (!constValue.equals(subject.getWrapped())) {
            report.addError(buildKeywordFailure(subject)
                    .message("%s does not match the const value", subject)
                    .build());
        }
        return report.isValid();
    }
}
