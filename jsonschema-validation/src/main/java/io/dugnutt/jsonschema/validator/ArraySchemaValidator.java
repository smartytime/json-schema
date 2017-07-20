package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.ObjectComparator;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static javax.json.JsonValue.ValueType;

public class ArraySchemaValidator extends SchemaValidator<ArraySchema> {

    public ArraySchemaValidator(ArraySchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
    }

    public ArraySchemaValidator(ArraySchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject) {
        List<ValidationError> failures = new ArrayList<>();

        if (!subject.is(ValueType.ARRAY) && schema.isRequiresArray()) {
            return buildTypeMismatchError(subject, JsonSchemaType.ARRAY).buildOptional();
        } else if (subject.is(ValueType.ARRAY)) {

            testItemCount(subject).ifPresent(failures::add);
            if (schema.isNeedsUniqueItems()) {
                testUniqueness(subject).ifPresent(failures::add);
            }
            failures.addAll(testItems(subject));
        }
        return ValidationError.collectErrors(schema, subject.getPath(), failures);
    }

    private Optional<ValidationError> testItemCount(final PathAwareJsonValue subject) {
        int actualLength = subject.arraySize();
        Integer minItems = schema.getMinItems();
        Integer maxItems = schema.getMaxItems();

        if (minItems != null && actualLength < minItems) {
            return buildKeywordFailure(subject, JsonSchemaKeyword.MIN_ITEMS)
                    .message("expected minimum item count: %s, found: %s", minItems, actualLength)
                    .buildOptional();
        }

        if (maxItems != null && maxItems < actualLength) {
            return buildKeywordFailure(subject, JsonSchemaKeyword.MAX_ITEMS)
                    .message("expected maximum item count: %s, found: %s", maxItems, actualLength)
                    .buildOptional();
        }
        return Optional.empty();
    }

    private List<ValidationError> testItems(final PathAwareJsonValue subject) {
        List<ValidationError> errors = new ArrayList<>();
        Schema allItemSchema = schema.getAllItemSchema();
        List<Schema> itemSchemas = schema.getItemSchemas();
        Schema schemaOfAdditionalItems = schema.getSchemaOfAdditionalItems();

        if (allItemSchema != null) {
            validateItemsAgainstSchema(IntStream.range(0, subject.arraySize()),
                    subject,
                    allItemSchema,
                    errors::add);
        } else if (itemSchemas != null) {
            int itemValidationUntil = Math.min(subject.arraySize(), itemSchemas.size());
            validateItemsAgainstSchema(IntStream.range(0, itemValidationUntil),
                    subject,
                    itemSchemas::get,
                    errors::add);
            if (schemaOfAdditionalItems != null) {
                validateItemsAgainstSchema(IntStream.range(itemValidationUntil, subject.arraySize()),
                        subject,
                        schemaOfAdditionalItems,
                        errors::add);
            }
        }

        Schema containsSchema = schema.getContainsSchema();
        if (containsSchema != null) {
            SchemaValidator<Schema> containsValidator = factory.createValidator(containsSchema);
            Optional<PathAwareJsonValue> containsValid = subject.streamArrayItems()
                    .filter(arrayItem -> !containsValidator.validate(arrayItem).isPresent())
                    .findAny();
            if (!containsValid.isPresent()) {
                errors.add(buildKeywordFailure(subject, JsonSchemaKeyword.CONTAINS)
                        .message("array does not contain at least 1 matching item")
                        .build());
            }
        }

        return errors;
    }

    private void validateItemsAgainstSchema(final IntStream indices, final PathAwareJsonValue items,
                                            final Schema schema,
                                            final Consumer<ValidationError> failureCollector) {
        validateItemsAgainstSchema(indices, items, i -> schema, failureCollector);
    }

    private void validateItemsAgainstSchema(final IntStream indices, final PathAwareJsonValue items,
                                            final IntFunction<Schema> schemaForIndex,
                                            final Consumer<ValidationError> failureCollector) {
        indices.forEach(i-> {
            Schema schema = schemaForIndex.apply(i);
            factory.createValidator(schema)
                    .validate(items.getItem(i))
                    .ifPresent(failureCollector);
        });
    }

    private Optional<ValidationError> testUniqueness(final PathAwareJsonValue subject) {
        if (subject.arraySize() == 0) {
            return Optional.empty();
        }
        Collection<JsonValue> uniqueItems = new ArrayList<>(subject.arraySize());

        JsonArray arrayItems = subject.asJsonArray();

        for (JsonValue item : arrayItems) {
            for (JsonValue contained : uniqueItems) {
                if (ObjectComparator.lexicalEquivalent(contained, item)) {
                    return buildKeywordFailure(subject, JsonSchemaKeyword.UNIQUE_ITEMS)
                            .message("array items are not unique")
                            .model(item)
                            .model(contained)
                            .buildOptional();
                }

            }
            uniqueItems.add(item);
        }
        return Optional.empty();
    }
}
