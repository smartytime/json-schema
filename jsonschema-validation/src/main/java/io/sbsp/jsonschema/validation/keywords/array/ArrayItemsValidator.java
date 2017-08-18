package io.sbsp.jsonschema.validation.keywords.array;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import java.util.List;

public class ArrayItemsValidator {
    public static KeywordValidator<ItemsKeyword> getArrayItemsValidator(ItemsKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        if (keyword.hasIndexedSchemas()) {
            final SchemaValidator additionItemValidator = keyword.getAdditionalItemSchema()
                    .map(factory::createValidator)
                    .orElse(null);
            final List<SchemaValidator> indexedValidators = keyword.getIndexedSchemas().stream()
                    .map(factory::createValidator)
                    .collect(ImmutableList.toImmutableList());
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
