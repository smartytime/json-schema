package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.ArrayKeywords;
import io.dugnutt.jsonschema.six.ObjectComparator;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StreamUtils;
import io.dugnutt.jsonschema.validator.ChainedValidator.ChainedValidatorBuilder;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONTAINS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_ITEMS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.UNIQUE_ITEMS;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;
import static javax.json.JsonValue.ValueType;

public class ArrayKeywordsValidatorFactory implements PartialValidatorFactory {

    private ArrayKeywordsValidatorFactory() {
    }

    public static ArrayKeywordsValidatorFactory arrayKeywordsValidator() {
        return new ArrayKeywordsValidatorFactory();
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        return schema.getArrayKeywords().isPresent();
    }

    @Override
    public Set<ValueType> appliesToTypes() {
        return Collections.singleton(ValueType.ARRAY);
    }

    @Override
    public SchemaValidator forSchema(Schema schema, SchemaValidatorFactory factory) {
        if (schema.getArrayKeywords().isPresent()) {
            ArrayKeywords keywords = schema.getArrayKeywords().get();
            ChainedValidatorBuilder validator = ChainedValidator.builder()
                    .schema(schema)
                    .factory(factory);

            // ########################################
            // MIN ITEMS
            // ########################################
            if (keywords.getMinItems() != null) {
                validator.addValidator(MIN_ITEMS, (subject, report) -> {
                    int actualLength = subject.arraySize();
                    int minItems = keywords.getMinItems();
                    if (actualLength < minItems) {
                        return report.addError(buildKeywordFailure(subject, schema, MIN_ITEMS)
                                .message("expected minimum item count: %s, found: %s", minItems, actualLength)
                                .build());
                    }
                    return true;
                });
            }

            // ########################################
            // MAX ITEMS
            // ########################################
            if (keywords.getMaxItems() != null) {
                validator.addValidator(MAX_ITEMS, (subject, report) -> {
                    int actualLength = subject.arraySize();
                    int maxItems = keywords.getMaxItems();
                    if (maxItems < actualLength) {
                        return report.addError(buildKeywordFailure(subject, schema, MAX_ITEMS)
                                .message("expected maximum item count: %s, found: %s", maxItems, actualLength)
                                .build());
                    }
                    return true;
                });
            }

            // ########################################
            // UNIQUE ITEMS
            // ########################################
            if (keywords.isNeedsUniqueItems()) {
                validator.addValidator(UNIQUE_ITEMS, (subject, report) -> {
                    if (subject.arraySize() == 0) {
                        return true;
                    }
                    Collection<JsonValue> uniqueItems = new ArrayList<>(subject.arraySize());

                    JsonArray arrayItems = subject.asJsonArray();

                    for (JsonValue item : arrayItems) {
                        for (JsonValue contained : uniqueItems) {
                            if (ObjectComparator.lexicalEquivalent(contained, item)) {
                                return report.addError(buildKeywordFailure(subject, schema, UNIQUE_ITEMS)
                                        .message("array items are not unique")
                                        .model(item)
                                        .model(contained)
                                        .build());
                            }
                        }
                        uniqueItems.add(item);
                    }
                    return true;
                });
            }

            // ########################################
            // ALL ITEMS
            // ########################################
            keywords.findAllItemSchema().ifPresent(allItemSchema -> {
                SchemaValidator allItemValidator = factory.createValidator(allItemSchema);
                validator.addValidator(ITEMS, (subject, report) ->
                        validateItemsAgainstSchema(report, IntStream.range(0, subject.arraySize()),
                                subject, allItemValidator));
            });

            // ########################################
            // ITEM SCHEMA
            // ########################################
            final List<Schema> itemSchemas = keywords.getItemSchemas();
            if (keywords.getItemSchemas().size() > 0) {
                final List<SchemaValidator> itemValidators = keywords.getItemSchemas().stream()
                        .map(factory::createValidator)
                        .collect(StreamUtils.toImmutableList());

                validator.addValidator(ITEMS, (subject, report) -> {
                    int itemValidationUntil = Math.min(subject.arraySize(), itemSchemas.size());
                    return validateItemsAgainstSchema(report, IntStream.range(0, itemValidationUntil),
                            subject,
                            itemValidators::get);
                });
            }

            // ########################################
            // ADDITIONAL ITEMS SCHEMA
            // ########################################
            if (itemSchemas.size() > 0 && keywords.findSchemaOfAdditionalItems().isPresent()) {
                validator.addValidator(ADDITIONAL_ITEMS, (subject, report) -> {
                    SchemaValidator additionalItemsValidator = factory.createValidator(keywords.getSchemaOfAdditionalItems());
                    int itemValidationUntil = Math.min(subject.arraySize(), itemSchemas.size());

                    return validateItemsAgainstSchema(report, IntStream.range(itemValidationUntil, subject.arraySize()),
                            subject, additionalItemsValidator);
                });
            }

            // ########################################
            // CONTAINS SCHEMA
            // ########################################
            keywords.findContainsSchema().ifPresent(containsSchema -> {
                validator.addValidator(CONTAINS, (subject, report) -> {
                    SchemaValidator containsValidator = factory.createValidator(containsSchema);
                    Optional<PathAwareJsonValue> containsValid = subject.getPathAwareArrayItems()
                            .filter(arrayItem -> !containsValidator.validate(arrayItem, report))
                            .findAny();
                    if (!containsValid.isPresent()) {
                        return report.addError(buildKeywordFailure(subject, schema, CONTAINS)
                                .message("array does not contain at least 1 matching item")
                                .build());
                    }
                    return true;
                });
            });

            return validator.build();
        } else {
            return SchemaValidator.NOOP_VALIDATOR;
        }
    }

    private boolean validateItemsAgainstSchema(ValidationReport report, final IntStream indices,
                                               final PathAwareJsonValue items, final SchemaValidator schema) {
        return validateItemsAgainstSchema(report, indices, items, i -> schema);
    }

    private boolean validateItemsAgainstSchema(ValidationReport report, final IntStream indices,
                                               final PathAwareJsonValue items, final IntFunction<SchemaValidator> schemaForValidator) {
        AtomicBoolean bool = new AtomicBoolean(true);
        indices.forEach(i -> {
            SchemaValidator validator = schemaForValidator.apply(i);
            boolean idxValid = validator.validate(items.getItem(i), report);
            bool.compareAndSet(true, idxValid);
        });
        return bool.get();
    }
}
