package io.sbsp.jsonschema.impl;

import io.sbsp.jsonschema.Draft3Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.Draft3Keywords;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.keyword.TypeKeyword;
import io.sbsp.jsonschema.utils.Schemas;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Draft3SchemaImpl extends JsonSchemaImpl<Draft3Schema> implements Draft3Schema {

    public Draft3SchemaImpl(SchemaLocation location, Map<KeywordInfo<?>, SchemaKeyword> keywords) {
        super(location, keywords, JsonSchemaVersion.Draft3);
    }

    // #####################################################
    // #####  KEYWORDS ONLY USED BY Draft3 -> Draft 4   ####
    // #####################################################

    @Override
    public boolean isAnyType() {
        return types().isEmpty();
    }

    @Override
    public Set<JsonSchemaType> getDisallow() {
        return keyword(Draft3Keywords.DISALLOW)
                .map(TypeKeyword::getDisallowedTypes).orElse(Collections.emptySet());
    }

    @Override
    public Optional<Schema> getExtendsSchema() {
        return keyword(Draft3Keywords.EXTENDS).map(SingleSchemaKeyword::getKeywordValue) ;
    }

    @Override
    public Boolean isRequired() {
        return keywordValue(Draft3Keywords.REQUIRED_DRAFT3).orElse(false);
    }

    @Override
    public Number getDivisibleBy() {
        return multipleOf();
    }

    // #####################################################
    // #####  KEYWORDS SHARED BY Draft3 -> Draft 4      ####
    // #####################################################

    public boolean isExclusiveMinimum() {
        return keyword(Keywords.MINIMUM).map(LimitKeyword::isExclusive).orElse(false);
    }

    public boolean isExclusiveMaximum() {
        return keyword(Keywords.MAXIMUM).map(LimitKeyword::isExclusive).orElse(false);
    }

    public boolean isAllowAdditionalItems() {
        return additionalItemsSchema().map(Schemas.isNullSchema()).orElse(true);
    }

    public boolean isAllowAdditionalProperties() {
        return additionalPropertiesSchema().map(Schemas.isNullSchema()).orElse(true);
    }

    @Override
    public Draft3Schema asType(Schema source) {
        return source.asDraft3();
    }
}
