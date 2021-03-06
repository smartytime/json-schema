package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.ObjectComparator;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.BooleanKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collection;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.UNIQUE_ITEMS;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class ArrayUniqueItemsValidator extends KeywordValidator<BooleanKeyword> {

    private final boolean requireUnique;

    public ArrayUniqueItemsValidator(BooleanKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.uniqueItems, schema);
        requireUnique = keyword.getKeywordValue();
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
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
                    report.addError(buildKeywordFailure(subject, schema, UNIQUE_ITEMS)
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
