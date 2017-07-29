package io.dugnutt.jsonschema.validator.keywords;

import io.dugnutt.jsonschema.six.ObjectComparator;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;

import javax.json.JsonArray;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class EnumValidator extends KeywordValidator {
    private final JsonArray enumValues;

    @Builder
    EnumValidator(Schema schema, JsonArray enumValues) {
        super(ENUM, schema);
        this.enumValues = checkNotNull(enumValues);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        boolean foundMatch = enumValues.stream()
                .anyMatch(val -> ObjectComparator.lexicalEquivalent(val, subject.getWrapped()));
        if (!foundMatch) {
            report.addError(buildKeywordFailure(subject, schema, ENUM)
                    .message("%s does not match the enum values", subject)
                    .build());
        }
        return report.isValid();
    }
}
