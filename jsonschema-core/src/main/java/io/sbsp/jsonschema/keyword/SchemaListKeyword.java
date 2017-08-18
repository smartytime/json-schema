package io.sbsp.jsonschema.keyword;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class SchemaListKeyword implements SchemaKeyword {
    private final List<Schema> schemas;

    @Builder(toBuilder = true)
    public SchemaListKeyword(@Singular @NonNull List<Schema> schemas) {
        checkNotNull(schemas, "schemas must not be null");
        this.schemas = ImmutableList.copyOf(schemas);
    }

    public static SchemaListKeyword newInstance() {
        return new SchemaListKeyword(Collections.emptyList());
    }

    @Override
    public void writeJson(KeywordInfo<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        generator.writeKey(keyword);
        generator.writeStartArray();
        for (Schema schema : schemas) {
            schema.asVersion(version).toJson(generator);
        }
        generator.writeEnd();
    }

    public List<Schema> getSchemas() {
        return schemas;
    }
}
