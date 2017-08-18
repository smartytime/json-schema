package io.sbsp.jsonschema.validation.keywords;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.ObjectComparator;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;

import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class EnumValidator extends KeywordValidator<JsonArrayKeyword> {
    private final List<JsonValue> enumValues;

    public EnumValidator(JsonArrayKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.ENUM, schema);
        this.enumValues = new ArrayList<>(keyword.getJsonArray());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        for (JsonValue enumValue : enumValues) {
            if (ObjectComparator.lexicalEquivalent(enumValue, subject.getWrapped())) {
                return true;
            }
        }
        report.addError(buildKeywordFailure(subject)
                .message("%s does not match the enum values", subject)
                .build());
        return report.isValid();
    }
}
