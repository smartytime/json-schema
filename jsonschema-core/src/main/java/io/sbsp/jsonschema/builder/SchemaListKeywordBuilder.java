package io.sbsp.jsonschema.builder;

import com.google.common.collect.Lists;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class SchemaListKeywordBuilder implements SchemaKeywordBuilder<SchemaListKeyword> {

    private final List<SchemaBuilder> indexSchemas = new ArrayList<>();

    public SchemaListKeywordBuilder withSchema(SchemaBuilder builder) {
        checkNotNull(builder, "builder must not be null");
        indexSchemas.add(builder);
        return this;
    }

    public List<SchemaBuilder> getSchemas() {
        return indexSchemas;
    }

    @Override
    public Stream<SchemaBuilder> getAllSchemas() {
        return indexSchemas.stream();
    }

    @Override
    public SchemaListKeyword build(SchemaLocation parentLocation, KeywordMetadata<?> keyword, LoadingReport report) {
        final SchemaLocation location = parentLocation.child(keyword.getKey());

        AtomicInteger i = new AtomicInteger(0);
        final List<Schema> schemas = Lists.transform(indexSchemas, builder -> {
            final SchemaLocation idxLocation = location.child(i.getAndIncrement());
            return builder.build(idxLocation, report);
        });

        return new SchemaListKeyword(schemas);
    }

    public SchemaListKeywordBuilder withSchemas(Collection<SchemaBuilder> schemas) {
        this.indexSchemas.clear();
        this.indexSchemas.addAll(schemas);
        return this;
    }
}
