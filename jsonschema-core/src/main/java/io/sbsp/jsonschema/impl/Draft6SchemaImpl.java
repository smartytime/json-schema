package io.sbsp.jsonschema.impl;

import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Draft6SchemaImpl extends JsonSchemaImpl<Draft6Schema> implements Draft6Schema {

    public Draft6SchemaImpl(SchemaLocation location, Map<KeywordInfo<?>, SchemaKeyword> keywords) {
        super(location, keywords, JsonSchemaVersion.Draft6);
    }

    // #####################################################
    // #####  KEYWORDS for Draft6    #######################
    // #####################################################

    @Override
    public JsonArray getExamples() {
        return examples();
    }

    @Override
    public Map<String, Schema> getDefinitions() {
        return definitions();
    }

    @Override
    public Optional<JsonValue> getConstValue() {
        return constValue();
    }

    @Override
    public Number getExclusiveMinimum() {
        return exclusiveMinimum();
    }

    @Override
    public Number getExclusiveMaximum() {
        return exclusiveMaximum();
    }

    @Override
    public Optional<Draft6Schema> getContainsSchema() {
        return containsSchema().map(this::asType);
    }

    @Override
    public Optional<Draft6Schema> getPropertyNameSchema() {
        return propertyNameSchema().map(this::asType);
    }

    // #####################################################
    // #####  KEYWORDS SHARED BY Draft4 -> Draft 6      ####
    // #####################################################

    @Override
    public Optional<Schema> getNotSchema() {
        return notSchema();
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
    public Draft6Schema asType(Schema source) {
        return source.asDraft6();
    }
}
