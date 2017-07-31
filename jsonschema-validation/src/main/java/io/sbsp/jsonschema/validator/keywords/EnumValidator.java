package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.ObjectComparator;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ENUM;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class EnumValidator extends KeywordValidator {
    private final List<JsonValue> enumValues;

    @Builder
    EnumValidator(Schema schema, JsonArray enumValues) {
        super(ENUM, schema);
        checkNotNull(enumValues);
        this.enumValues = new ArrayList<>(enumValues);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        for (JsonValue enumValue : enumValues) {
            if (ObjectComparator.lexicalEquivalent(enumValue, subject.getWrapped())) {
                return true;
            }
        }
        report.addError(buildKeywordFailure(subject, schema, ENUM)
                .message("%s does not match the enum values", subject)
                .build());
        return report.isValid();
    }
}
