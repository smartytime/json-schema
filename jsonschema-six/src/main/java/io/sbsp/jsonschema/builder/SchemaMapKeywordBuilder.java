package io.sbsp.jsonschema.builder;

import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchemaMapKeywordBuilder implements SchemaKeywordBuilder<SchemaMapKeyword> {

    private final Map<String, JsonSchemaBuilder> schemaMap;

    public SchemaMapKeywordBuilder(String key, JsonSchemaBuilder schemaMap) {
        checkNotNull(key, "key must not be null");
        checkNotNull(schemaMap, "schemaMap must not be null");
        this.schemaMap = ImmutableMap.of(key, schemaMap);
    }

    public SchemaMapKeywordBuilder(Map<String, ? extends JsonSchemaBuilder> keywordValues) {
        checkNotNull(keywordValues, "keywordValues must not be null");
        this.schemaMap = Collections.unmodifiableMap(keywordValues);
    }

    public Map<String, JsonSchemaBuilder> getSchemas() {
        return schemaMap;
    }

    public SchemaMapKeywordBuilder addSchema(String key, JsonSchemaBuilder anotherValue) {
        checkNotNull(anotherValue, "anotherValue must not be null");
        final Map<String, JsonSchemaBuilder> items = new LinkedHashMap<String, JsonSchemaBuilder>(schemaMap);
        items.put(key, anotherValue);
        return new SchemaMapKeywordBuilder(items);
    }

    @Override
    public SchemaMapKeyword build(SchemaLocation location, @Nullable SchemaFactory factory, @Nullable JsonObject rootDocument) {
        Map<String, Schema> keywordMap = new LinkedHashMap<>();
        for (Map.Entry<String, JsonSchemaBuilder> entry : schemaMap.entrySet()) {
            final SchemaLocation childLocationForKey = location.child(entry.getKey());
            if (factory != null) {
                final Optional<Schema> cachedSchema = factory.findCachedSchema(location.getUniqueURI());
                if (cachedSchema.isPresent()) {
                    keywordMap.put(entry.getKey(), cachedSchema.get());
                    continue;
                }
            }
            final JsonSchemaBuilder childBuilder = entry.getValue();
            keywordMap.put(entry.getKey(), childBuilder.schemaFactory(factory)
                    .currentDocument(rootDocument)
                    .build(childLocationForKey));
        }
        return new SchemaMapKeyword(keywordMap);


    }
}
