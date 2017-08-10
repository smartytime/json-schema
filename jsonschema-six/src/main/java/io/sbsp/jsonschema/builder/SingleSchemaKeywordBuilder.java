package io.sbsp.jsonschema.builder;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class SingleSchemaKeywordBuilder implements SchemaKeywordBuilder<SingleSchemaKeyword> {

    private final JsonSchemaBuilder schemaBuilder;

    public SingleSchemaKeywordBuilder(JsonSchemaBuilder schema) {
        this.schemaBuilder = checkNotNull(schema);
    }

    @Override
    public SingleSchemaKeyword build(SchemaLocation location, @Nullable SchemaFactory factory, @Nullable JsonObject rootDocument) {

        if (factory != null) {
            final Optional<Schema> cachedSchema = factory.findCachedSchema(location.getUniqueURI());
            if (cachedSchema.isPresent()) {
                return new SingleSchemaKeyword(cachedSchema.get());
            }
        }

        return new SingleSchemaKeyword(schemaBuilder
                .schemaFactory(factory)
                .currentDocument(rootDocument)
                .build(location));
    }
}
