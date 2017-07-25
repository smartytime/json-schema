package io.dugnutt.jsonschema.six;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

@Builder
@Getter
@EqualsAndHashCode
public class JsonSchemaInfo {

    public JsonSchemaInfo(SchemaLocation location, SchemaLocation containedBy, String propertyName, JsonSchemaKeyword keyword, Integer index) {
        this.containedBy = containedBy;
        SchemaLocation loc = containedBy;
        if (index != null) {
            loc = loc.withChildPath(keyword.key(), index);
        } else if (propertyName != null) {
            loc = loc.withChildPath(keyword).withChildPath(propertyName);
        } else if (keyword != null) {
            loc = loc.withChildPath();
        }
        this.location = loc;
        this.propertyName = propertyName;
        this.keyword = keyword;
        this.index = index;
    }

    private final SchemaLocation location;
    private final SchemaLocation containedBy;

    @Nullable
    private final String propertyName;

    @Nullable
    private final JsonSchemaKeyword keyword;

    @Nullable
    private final Integer index;

    public static JsonSchemaInfo indexSchema(SchemaLocation containedBy, JsonSchemaKeyword keyword, int idx) {
        return builder()
                .containedBy(containedBy)
                .keyword(keyword)
                .index(idx)
                .build();
    }

    public static JsonSchemaInfo keywordSchemaInfo(SchemaLocation containedBy, JsonSchemaKeyword keyword) {
        return builder()
                .containedBy(containedBy)
                .keyword(keyword)
                .keyword(keyword)
                .build();
    }

    public static JsonSchemaInfo propertySchema(SchemaLocation containedBy, JsonSchemaKeyword keyword, String property) {
        return builder()
                .containedBy(containedBy)
                .keyword(keyword)
                .propertyName(property)
                .build();
    }

    public static JsonSchemaInfo rootSchema() {
        final SchemaLocation schemaLocation = SchemaLocation.anonymousRoot();
        return builder().location(schemaLocation).containedBy(schemaLocation).build();
    }

    public static JsonSchemaInfo rootSchema(String id) {
        checkNotNull(id, "id must not be null");
        final SchemaLocation schemaLocation = SchemaLocation.schemaLocation(id);
        return builder().location(schemaLocation).containedBy(schemaLocation).build();
    }
}
