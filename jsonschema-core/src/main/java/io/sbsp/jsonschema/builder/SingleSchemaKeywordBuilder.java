package io.sbsp.jsonschema.builder;

import com.google.common.collect.Streams;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Wither;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class SingleSchemaKeywordBuilder implements SchemaKeywordBuilder<SingleSchemaKeyword> {

    @Wither
    @NonNull
    private final SchemaBuilder schemaBuilder;

    public SingleSchemaKeywordBuilder() {
        this.schemaBuilder = JsonSchemaBuilder.jsonSchema();
    }

    @Override
    public SingleSchemaKeyword build(SchemaLocation parentLocation, KeywordMetadata<?> keyword, LoadingReport report) {
        final SchemaLocation location = parentLocation.child(keyword.getKey());
        return new SingleSchemaKeyword(schemaBuilder.build(location, report));
    }

    @Override
    public Stream<SchemaBuilder> getAllSchemas() {
        return Streams.stream(Optional.ofNullable(schemaBuilder));
    }
}
