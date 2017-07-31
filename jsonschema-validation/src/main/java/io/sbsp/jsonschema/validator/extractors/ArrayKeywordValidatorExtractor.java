package io.sbsp.jsonschema.validator.extractors;

import io.sbsp.jsonschema.six.keywords.ArrayKeywords;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.utils.StreamUtils;
import io.sbsp.jsonschema.validator.extractors.KeywordValidators.KeywordValidatorsBuilder;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.keywords.array.ArrayContainsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayItemValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayMaxItemsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayMinItemsValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayPerItemValidator;
import io.sbsp.jsonschema.validator.keywords.array.ArrayUniqueItemsValidator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static javax.json.JsonValue.ValueType;

public class ArrayKeywordValidatorExtractor implements KeywordValidatorExtractor {

    private ArrayKeywordValidatorExtractor() {
    }

    public static ArrayKeywordValidatorExtractor arrayKeywordsValidator() {
        return new ArrayKeywordValidatorExtractor();
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        return schema.hasArrayKeywords();
    }

    @Override
    public Set<ValueType> getApplicableTypes() {
        return Collections.singleton(ValueType.ARRAY);
    }

    @Override
    public KeywordValidators getKeywordValidators(Schema schema, SchemaValidatorFactory factory) {
        KeywordValidatorsBuilder validator = KeywordValidators.builder()
                .schema(schema)
                .validatorFactory(factory);

        if (schema.hasArrayKeywords()) {
            ArrayKeywords keywords = schema.getArrayKeywords();
            // ########################################
            // MIN ITEMS
            // ########################################
            if (keywords.getMinItems() != null) {
                validator.addValidator(ArrayMinItemsValidator.builder()
                        .minItems(keywords.getMinItems())
                        .schema(schema).build());
            }

            // ########################################
            // MAX ITEMS
            // ########################################
            if (keywords.getMaxItems() != null) {
                validator.addValidator(ArrayMaxItemsValidator.builder()
                        .schema(schema)
                        .maxItems(keywords.getMaxItems())
                        .build());
            }

            // ########################################
            // UNIQUE ITEMS
            // ########################################
            if (keywords.isNeedsUniqueItems()) {
                validator.addValidator(ArrayUniqueItemsValidator.builder()
                        .schema(schema)
                        .build());
            }

            // ########################################
            // ALL ITEMS
            // ########################################
            keywords.findAllItemSchema().ifPresent(allItemSchema -> {
                SchemaValidator allItemValidator = factory.createValidator(allItemSchema);
                validator.addValidator(ArrayItemValidator.builder()
                        .schema(schema)
                        .allItemValidator(allItemValidator)
                        .build());
            });

            // ########################################
            // PER ITEM SCHEMA
            // ########################################
            if (keywords.getItemSchemas().size() > 0) {
                final List<SchemaValidator> itemValidators = keywords.getItemSchemas().stream()
                        .map(factory::createValidator)
                        .collect(StreamUtils.toImmutableList());

                final SchemaValidator additionalItemsValidator = keywords.findSchemaOfAdditionalItems()
                        .map(factory::createValidator)
                        .orElse(null);

                validator.addValidator(ArrayPerItemValidator.builder()
                        .schema(schema)
                        .indexedValidators(itemValidators)
                        .additionalItemValidator(additionalItemsValidator)
                        .build());
            }

            // ########################################
            // CONTAINS SCHEMA
            // ########################################
            keywords.findContainsSchema().ifPresent(containsSchema -> {
                final SchemaValidator containsValidator = factory.createValidator(containsSchema);
                validator.addValidator(ArrayContainsValidator.builder()
                        .schema(schema)
                        .containsValidator(containsValidator)
                        .build());
            });
        }
        return validator.build();
    }
}