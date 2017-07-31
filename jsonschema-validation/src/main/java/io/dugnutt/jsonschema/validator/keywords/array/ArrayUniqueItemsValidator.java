package io.dugnutt.jsonschema.validator.keywords.array;

import io.dugnutt.jsonschema.six.ObjectComparator;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collection;

import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.UNIQUE_ITEMS;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayUniqueItemsValidator extends KeywordValidator {

    @Builder
    public ArrayUniqueItemsValidator(Schema schema) {
        super(UNIQUE_ITEMS, schema);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        if (subject.arraySize() == 0) {
            return true;
        }
        Collection<JsonValue> uniqueItems = new ArrayList<>(subject.arraySize());
        JsonArray arrayItems = subject.asJsonArray();

        for (JsonValue item : arrayItems) {
            for (JsonValue contained : uniqueItems) {
                if (ObjectComparator.lexicalEquivalent(contained, item)) {
                    report.addError(buildKeywordFailure(subject, schema, UNIQUE_ITEMS)
                            .message("array items are not unique")
                            .model(item)
                            .model(contained)
                            .build());
                    return false;
                }
            }
            uniqueItems.add(item);
        }
        return true;
    }
}
