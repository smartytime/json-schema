package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.ArraySchema;
import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.ObjectComparator;
import org.everit.jsonschema.api.Schema;
import org.everit.json.JsonArray;
import org.everit.json.JsonElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class ArraySchemaValidator extends SchemaValidator<ArraySchema> {

    public ArraySchemaValidator(ArraySchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonElement<?> toBeValidated) {

        List<ValidationError> failures = new ArrayList<>();
        if (toBeValidated.schemaType() != JsonSchemaType.Array && schema.isRequiresArray()) {
            return Optional.of(failure(JsonSchemaType.Array, toBeValidated.schemaType()));
        } else if(toBeValidated.schemaType() == JsonSchemaType.Array) {
            JsonArray arrSubject = (JsonArray) toBeValidated;
            testItemCount(arrSubject).ifPresent(failures::add);
            if (schema.isNeedsUniqueItems()) {
                testUniqueness(arrSubject).ifPresent(failures::add);
            }
            failures.addAll(testItems(arrSubject));
        }
        return ValidationError.collectErrors(schema, failures);
    }

    private Optional<ValidationError> testItemCount(final JsonArray subject) {
        int actualLength = subject.length();
        Integer minItems = schema.getMinItems();
        Integer maxItems = schema.getMaxItems();

        if (minItems != null && actualLength < minItems) {
            return Optional.of(failure("expected minimum item count: " + minItems
                    + ", found: " + actualLength, "minItems"));
        }

        if (maxItems != null && maxItems < actualLength) {
            return Optional.of(failure("expected maximum item count: " + maxItems
                    + ", found: " + actualLength, "maxItems"));
        }
        return Optional.empty();
    }

    private List<ValidationError> testItems(final JsonArray subject) {
        List<ValidationError> rval = new ArrayList<>();
        Schema allItemSchema = schema.getAllItemSchema();
        List<Schema> itemSchemas = schema.getItemSchemas();
        boolean additionalItems = schema.isNeedsAdditionalItems();
        Schema schemaOfAdditionalItems = schema.getSchemaOfAdditionalItems();
        if (allItemSchema != null) {
            validateItemsAgainstSchema(IntStream.range(0, subject.length()),
                    subject,
                    allItemSchema,
                    rval::add);
        } else if (itemSchemas != null) {
            if (!additionalItems && subject.length() > itemSchemas.size()) {
                rval.add(failure(String.format("expected: [%d] array items, found: [%d]",
                        itemSchemas.size(), subject.length()), "items"));
            }
            int itemValidationUntil = Math.min(subject.length(), itemSchemas.size());
            validateItemsAgainstSchema(IntStream.range(0, itemValidationUntil),
                    subject,
                    itemSchemas::get,
                    rval::add);
            if (schemaOfAdditionalItems != null) {
                validateItemsAgainstSchema(IntStream.range(itemValidationUntil, subject.length()),
                        subject,
                        schemaOfAdditionalItems,
                        rval::add);
            }
        }
        return rval;
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
            SchemaValidatorFactory.findValidator(schema)
                    .validate(items.get(i))
                    .map(exc -> exc.prepend(copyOfI))
                    .ifPresent(failureCollector);
        }
    }

    private Optional<ValidationError> testUniqueness(final JsonArray<?> subject) {
        if (subject.length() == 0) {
            return Optional.empty();
        }
        Collection<JsonElement<?>> uniqueItems = new ArrayList<>(subject.length());

        for (JsonElement<?> item : subject) {
            for (JsonElement<?> contained : uniqueItems) {
                if (ObjectComparator.deepEquals(contained, item)) {
                    return Optional.of(
                            failure("array items are not unique", "uniqueItems"));
                }
            }
            uniqueItems.add(item);
        }
        return Optional.empty();
    }

}
