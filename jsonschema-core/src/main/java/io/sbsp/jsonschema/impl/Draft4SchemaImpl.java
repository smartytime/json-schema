package io.sbsp.jsonschema.impl;

import io.sbsp.jsonschema.Draft4Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.utils.Schemas;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Draft4SchemaImpl extends JsonSchemaImpl<Draft4Schema> implements Draft4Schema {

    public Draft4SchemaImpl(SchemaLocation location, Map<KeywordMetadata<?>, SchemaKeyword> keywords) {
        super(location, keywords, JsonSchemaVersion.Draft4);
    }

    // #####################################################
    // #####  KEYWORDS SHARED BY Draft3 -> Draft 4      ####
    // #####################################################

    public boolean isExclusiveMinimum() {
        return keyword(Keywords.minimum).map(LimitKeyword::isExclusive).orElse(false);
    }

    public boolean isExclusiveMaximum() {
        return keyword(Keywords.maximum).map(LimitKeyword::isExclusive).orElse(false);
    }

    public boolean isAllowAdditionalItems() {
        return additionalItemsSchema().map(Schemas.isNullSchema()).orElse(true);
    }

    public boolean isAllowAdditionalProperties() {
        return additionalPropertiesSchema().map(Schemas.isNullSchema()).orElse(true);
    }

    // #####################################################
    // #####  KEYWORDS SHARED BY Draft4 -> Draft 6      ####
    // #####################################################

    @Override
    public Optional<Draft4Schema> getNotSchema() {
        return notSchema().map(this::asType);
    }

    @Override
    public List<Schema> getAllOfSchemas() {
        return allOfSchemas();
    }

    @Override
    public List<Schema> getAnyOfSchemas() {
        return anyOfSchemas();
    }

    @Override
    public List<Schema> getOneOfSchemas() {
        return oneOfSchemas();
    }

    @Override
    public Number getMultipleOf() {
        return multipleOf();
    }

    @Override
    public Integer getMaxProperties() {
        return maxProperties();
    }

    @Override
    public Integer getMinProperties() {
        return minProperties();
    }

    @Override
    public Set<String> getRequiredProperties() {
        return requiredProperties();
    }

    @Override
    public Draft4Schema asType(Schema source) {
        return source.asDraft4();
    }
}
