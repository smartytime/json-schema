package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;

import javax.json.JsonArray;


public class JsonArrayKeyword extends SchemaKeywordImpl<JsonArray> {
    public JsonArrayKeyword(JsonArray enumValues, JsonSchemaKeywordType keyword) {
        super(enumValues);
    }

    public JsonArrayKeyword(JsonArray enumValues) {
        super(enumValues);
    }

    public JsonArray getJsonArray() {
        return value();
    }
}
