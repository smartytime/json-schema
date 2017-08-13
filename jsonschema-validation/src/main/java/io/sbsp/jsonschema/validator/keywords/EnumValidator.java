package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.ObjectComparator;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;

import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ENUM;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class EnumValidator extends KeywordValidator<JsonArrayKeyword> {
    private final List<JsonValue> enumValues;

    public EnumValidator(JsonArrayKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.$enum, schema);
        this.enumValues = new ArrayList<>(keyword.getJsonArray());
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
