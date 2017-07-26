package io.dugnutt.jsonschema.validator;

import com.google.common.base.Preconditions;
import io.dugnutt.jsonschema.six.ArrayKeywords;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.ObjectComparator;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;
import static javax.json.JsonValue.ValueType;

public class ArrayKeywordsValidator implements PartialSchemaValidator {

    public static ArrayKeywordsValidator arrayKeywordsValidator() {
        return new ArrayKeywordsValidator();
    }

    private ArrayKeywordsValidator() {
    }

    @Override
    public PartialSchemaValidator forSchema(Schema schema, SchemaValidatorFactory factory) {
        return this;
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        return schema.getArrayKeywords().isPresent();
    }

    @Override
    public boolean appliesToValue(PathAwareJsonValue value) {
        return value.is(ValueType.ARRAY);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject, Schema schema, SchemaValidatorFactory factory) {
        Preconditions.checkArgument(subject.is(ValueType.ARRAY), "Requires JsonArray as input");
        ArrayKeywords keywords = schema.getArrayKeywords()
                .orElseThrow(() -> new IllegalArgumentException("Schema must have array keywords"));

        List<ValidationError> failures = new ArrayList<>();

        testItemCount(subject, schema, keywords).ifPresent(failures::add);
        testUniqueness(subject, schema, keywords).ifPresent(failures::add);

        testItems(subject, schema, keywords, factory).forEach(failures::add);

        return ValidationError.collectErrors(schema, subject.getPath(), failures);
    }

    private Optional<ValidationError> testItemCount(final PathAwareJsonValue subject, Schema schema, ArrayKeywords keywords) {
        int actualLength = subject.arraySize();
        Integer minItems = keywords.getMinItems();
        Integer maxItems = keywords.getMaxItems();

        if (minItems != null && actualLength < minItems) {
            return buildKeywordFailure(subject, schema, JsonSchemaKeyword.MIN_ITEMS)
                    .message("expected minimum item count: %s, found: %s", minItems, actualLength)
                    .buildOptional();
        }

        if (maxItems != null && maxItems < actualLength) {
            return buildKeywordFailure(subject, schema, JsonSchemaKeyword.MAX_ITEMS)
                    .message("expected maximum item count: %s, found: %s", maxItems, actualLength)
                    .buildOptional();
        }
        return Optional.empty();
    }

    private List<ValidationError> testItems(final PathAwareJsonValue subject, Schema schema, ArrayKeywords keywords, SchemaValidatorFactory factory) {
        List<ValidationError> errors = new ArrayList<>();

        List<Schema> itemSchemas = keywords.getItemSchemas();

        keywords.findAllItemSchema().ifPresent(allItemSchema->{
            validateItemsAgainstSchema(factory, IntStream.range(0, subject.arraySize()),
                    subject,
                    allItemSchema,
                    errors::add);
        });

        if (itemSchemas.size() > 0) {
            int itemValidationUntil = Math.min(subject.arraySize(), itemSchemas.size());
            validateItemsAgainstSchema(factory, IntStream.range(0, itemValidationUntil),
                    subject,
                    itemSchemas::get,
                    errors::add);

            keywords.findSchemaOfAdditionalItems().ifPresent(schemaOfAdditionalItems->{
                validateItemsAgainstSchema(factory, IntStream.range(itemValidationUntil, subject.arraySize()),
                        subject,
                        schemaOfAdditionalItems,
                        errors::add);
            });
        }

        keywords.findContainsSchema().ifPresent(containsSchema->{
            JsonSchemaValidator containsValidator = factory.createValidator(containsSchema);
            Optional<PathAwareJsonValue> containsValid = subject.getPathAwareArrayItems()
                    .filter(arrayItem -> !containsValidator.validate(arrayItem).isPresent())
                    .findAny();
            if (!containsValid.isPresent()) {
                errors.add(buildKeywordFailure(subject, schema, JsonSchemaKeyword.CONTAINS)
                        .message("array does not contain at least 1 matching item")
                        .build());
            }
        });

        return errors;
    }

    private void validateItemsAgainstSchema(final SchemaValidatorFactory factory, final IntStream indices, final PathAwareJsonValue items,
                                            final Schema schema, final Consumer<ValidationError> failureCollector) {
        validateItemsAgainstSchema(factory, indices, items, i -> schema, failureCollector);
    }

    private void validateItemsAgainstSchema(final SchemaValidatorFactory factory, final IntStream indices, final PathAwareJsonValue items,
                                            final IntFunction<Schema> schemaForIndex,
                                            final Consumer<ValidationError> failureCollector) {
        indices.forEach(i-> {
            Schema schema = schemaForIndex.apply(i);
            factory.createValidator(schema)
                    .validate(items.getItem(i))
                    .ifPresent(failureCollector);
        });
    }

    private Optional<ValidationError> testUniqueness(final PathAwareJsonValue subject, final Schema schema, final ArrayKeywords arrayKeywords) {
        if (arrayKeywords.isNeedsUniqueItems()) {
            if (subject.arraySize() == 0) {
                return Optional.empty();
            }
            Collection<JsonValue> uniqueItems = new ArrayList<>(subject.arraySize());

            JsonArray arrayItems = subject.asJsonArray();

            for (JsonValue item : arrayItems) {
                for (JsonValue contained : uniqueItems) {
                    if (ObjectComparator.lexicalEquivalent(contained, item)) {
                        return buildKeywordFailure(subject, schema, JsonSchemaKeyword.UNIQUE_ITEMS)
                                .message("array items are not unique")
                                .model(item)
                                .model(contained)
                                .buildOptional();
                    }

                }
                uniqueItems.add(item);
            }
        }
        return Optional.empty();
    }
}
