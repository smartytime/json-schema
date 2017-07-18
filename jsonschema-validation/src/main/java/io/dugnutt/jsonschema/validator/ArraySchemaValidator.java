package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.ObjectComparator;
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

    public ArraySchemaValidator(ArraySchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue subject) {
        List<ValidationError> failures = new ArrayList<>();
        final ValueType valueType = subject.getValueType();
        if (valueType != ValueType.ARRAY && schema.isRequiresArray()) {
            return Optional.of(failure(JsonSchemaType.ARRAY, subject));
        } else if(valueType == ValueType.ARRAY) {
            JsonArray array = (JsonArray) subject;
            testItemCount(array).ifPresent(failures::add);
            if (schema.isNeedsUniqueItems()) {
                testUniqueness(array).ifPresent(failures::add);
            }
            failures.addAll(testItems(array));
        }
        return ValidationError.collectErrors(schema, failures);
    }

    private Optional<ValidationError> testItemCount(final JsonArray subject) {
        int actualLength = subject.size();
        Integer minItems = schema.getMinItems();
        Integer maxItems = schema.getMaxItems();

        if (minItems != null && actualLength < minItems) {
            return Optional.of(failure("expected minimum item count: " + minItems
                    + ", found: " + actualLength, JsonSchemaKeyword.MIN_ITEMS));
        }

        if (maxItems != null && maxItems < actualLength) {
            return Optional.of(failure("expected maximum item count: " + maxItems
                    + ", found: " + actualLength, JsonSchemaKeyword.MAX_ITEMS));
        }
        return Optional.empty();
    }

    private List<ValidationError> testItems(final JsonArray subject) {
        List<ValidationError> errors = new ArrayList<>();
        Schema allItemSchema = schema.getAllItemSchema();
        List<Schema> itemSchemas = schema.getItemSchemas();
        boolean additionalItems = schema.isPermitsAdditionalItems();
        Schema schemaOfAdditionalItems = schema.getSchemaOfAdditionalItems();

        if (allItemSchema != null) {
            validateItemsAgainstSchema(IntStream.range(0, subject.size()),
                    subject,
                    allItemSchema,
                    errors::add);
        } else if (itemSchemas != null) {
            if (!additionalItems && subject.size() > itemSchemas.size()) {
                errors.add(failure(String.format("expected: [%d] array items, found: [%d]",
                        itemSchemas.size(), subject.size()), JsonSchemaKeyword.ITEMS));
            }
            int itemValidationUntil = Math.min(subject.size(), itemSchemas.size());
            validateItemsAgainstSchema(IntStream.range(0, itemValidationUntil),
                    subject,
                    itemSchemas::get,
                    errors::add);
            if (schemaOfAdditionalItems != null) {
                validateItemsAgainstSchema(IntStream.range(itemValidationUntil, subject.size()),
                        subject,
                        schemaOfAdditionalItems,
                        errors::add);
            }
        }

        Schema containsSchema = schema.getContainsSchema();
        if (containsSchema != null) {
            SchemaValidator<Schema> containsValidator = context.getFactory().createValidator(containsSchema);
            Optional<JsonValue> containsValid = subject.stream()
                    .filter(arrayItem -> !containsValidator.validate(arrayItem).isPresent())
                    .findAny();
            if (!containsValid.isPresent()) {
                errors.add(failure("array does not contain at least 1 matching item", JsonSchemaKeyword.CONTAINS));
            }
        }

        return errors;
    }

    private void validateItemsAgainstSchema(final IntStream indices, final JsonArray items,
                                            final Schema schema,
                                            final Consumer<ValidationError> failureCollector) {
        validateItemsAgainstSchema(indices, items, i -> schema, failureCollector);
    }

    private void validateItemsAgainstSchema(final IntStream indices, final JsonArray items,
                                            final IntFunction<Schema> schemaForIndex,
                                            final Consumer<ValidationError> failureCollector) {
        for (int i : indices.toArray()) {
            String copyOfI = String.valueOf(i); // i is not effectively final so we copy it

            Schema schema = schemaForIndex.apply(i);
            SchemaValidatorFactory.createValidatorForSchema(schema)
                    .validate(items.get(i))
                    .map(exc -> exc.prepend(copyOfI))
                    .ifPresent(failureCollector);
        }
    }

    private Optional<ValidationError> testUniqueness(final JsonArray subject) {
        if (subject.size() == 0) {
            return Optional.empty();
        }
        Collection<JsonValue> uniqueItems = new ArrayList<>(subject.size());

        for (JsonValue item : subject) {
            for (JsonValue contained : uniqueItems) {
                if (ObjectComparator.lexicalEquivalent(contained, item)) {
                    return Optional.of(
                            failure("array items are not unique", JsonSchemaKeyword.UNIQUE_ITEMS));
                }
            }
            uniqueItems.add(item);
        }
        return Optional.empty();
    }

}