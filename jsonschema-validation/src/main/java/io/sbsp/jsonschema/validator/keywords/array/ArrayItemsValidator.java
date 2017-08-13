package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import java.util.List;

public class ArrayItemsValidator {
    public static KeywordValidator<ItemsKeyword> getArrayItemsValidator(ItemsKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        if (keyword.hasIndexedSchemas()) {
            final SchemaValidator additionItemValidator = keyword.getAdditionalItemSchema()
                    .map(factory::createValidator)
                    .orElse(null);
            final List<SchemaValidator> indexedValidators = factory.createValidators(keyword.getIndexedSchemas());
            return ArrayPerItemValidator.builder()
                    .schema(schema)
                    .indexedValidators(indexedValidators)
                    .additionalItemValidator(additionItemValidator)
                    .build();
        } else if (keyword.getAllItemSchema().isPresent()) {
            final SchemaValidator allItemValidator = factory.createValidator(keyword.getAllItemSchema().get());
            return ArrayItemValidator.builder()
                    .allItemValidator(allItemValidator)
                    .parentSchema(schema)
                    .build();
        } else {

            return null;
        }
    }
}
