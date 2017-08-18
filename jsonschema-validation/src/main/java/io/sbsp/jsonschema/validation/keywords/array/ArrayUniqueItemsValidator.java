package io.sbsp.jsonschema.validation.keywords.array;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.ObjectComparator;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.BooleanKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collection;

public class ArrayUniqueItemsValidator extends KeywordValidator<BooleanKeyword> {

    private final boolean requireUnique;

    public ArrayUniqueItemsValidator(BooleanKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.UNIQUE_ITEMS, schema);
        requireUnique = keyword.getKeywordValue();
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        if (!requireUnique) {
            return true;
        }
        if (subject.arraySize() == 0) {
            return true;
        }
        Collection<JsonValue> uniqueItems = new ArrayList<>(subject.arraySize());
        JsonArray arrayItems = subject.asJsonArray();

        for (JsonValue item : arrayItems) {
            for (JsonValue contained : uniqueItems) {
                if (ObjectComparator.lexicalEquivalent(contained, item)) {
                    report.addError(buildKeywordFailure(subject)
                            .message("array items are not unique")
                            .argument(item)
                            .argument(contained)
                            .build());
                    return false;
                }
            }
            uniqueItems.add(item);
        }
        return true;
    }
}
