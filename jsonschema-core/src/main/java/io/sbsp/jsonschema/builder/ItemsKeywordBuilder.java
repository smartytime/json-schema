package io.sbsp.jsonschema.builder;

import com.google.common.collect.Streams;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.sbsp.jsonschema.loading.LoadingIssues.conflictingKeyword;
import static io.sbsp.jsonschema.loading.LoadingIssues.invalidKeywordValue;

@EqualsAndHashCode
@Builder(toBuilder = true)
public class ItemsKeywordBuilder implements SchemaKeywordBuilder<ItemsKeyword> {

    private ItemsKeywordBuilder(List<SchemaBuilder> indexSchemas, SchemaBuilder additionalItemSchema, SchemaBuilder allItemSchema) {
        this.indexSchemas = indexSchemas;
        this.additionalItemSchema = additionalItemSchema;
        this.allItemSchema = allItemSchema;
    }

    @Singular
    @NonNull
    private final List<SchemaBuilder> indexSchemas;
    private SchemaBuilder additionalItemSchema;
    private SchemaBuilder allItemSchema;

    public List<SchemaBuilder> getSchemas() {
        return indexSchemas;
    }

    public static ItemsKeywordBuilder newInstance() {
        return builder().build();
    }

    @Override
    public ItemsKeyword build(SchemaLocation parentLocation, KeywordMetadata<?> keyword, LoadingReport report) {

        final SchemaLocation itemsLocation = parentLocation.child(JsonSchemaKeywordType.ITEMS);
        final SchemaLocation additionalItemsLocation = parentLocation.child(JsonSchemaKeywordType.ADDITIONAL_ITEMS);

        if (allItemSchema().isPresent() && additionalItemSchema().isPresent()) {
            report.warn(conflictingKeyword(Keywords.items, Keywords.additionalItems)
                    .location(parentLocation)
                    .argument("type = object")
                    .resolutionMessage("The additionalItems schema will not be used")
            );
        }

        // Build all the index schemas
        AtomicInteger i = new AtomicInteger(0);
        final List<Schema> builtSchemas = indexSchemas.stream()
                .map(idxSchemaBuilder -> {
                    final SchemaLocation idxLocation = itemsLocation.child(i.getAndIncrement());
                    return idxSchemaBuilder.build(idxLocation, report);
                }).collect(Collectors.toList());

        // Validate that we're not mixing index schemas and allItemSchema.  This can happen with the builder, but not from loading.
        if (!builtSchemas.isEmpty() && allItemSchema().isPresent()) {
            report.warn(invalidKeywordValue(Keywords.items, "You can't have per-index validators and a global array validator")
                    .location(parentLocation)
                    .resolutionMessage("The global array validator will be used")
            );
            builtSchemas.clear();
        }

        // Build the two schemas, if necessary
        Schema allItemSchema = allItemSchema().map(builder -> builder.build(itemsLocation, report)).orElse(null);
        Schema additionalItemSchema = additionalItemSchema().map(builder -> builder.build(additionalItemsLocation, report)).orElse(null);

        return new ItemsKeyword(allItemSchema, additionalItemSchema, builtSchemas);
    }

    protected Optional<SchemaBuilder> allItemSchema() {
        return Optional.ofNullable(allItemSchema);
    }

    protected Optional<SchemaBuilder> additionalItemSchema() {
        return Optional.ofNullable(additionalItemSchema);
    }

    @Override
    public Stream<SchemaBuilder> getAllSchemas() {
        return Streams.concat(indexSchemas.stream(), Stream.of(additionalItemSchema, allItemSchema))
                .filter(Objects::nonNull);
    }
}
