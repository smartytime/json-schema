package io.sbsp.jsonschema.keyword;

import javax.json.JsonArray;


public class JsonArrayKeyword extends SchemaKeywordImpl<JsonArray> {

    public JsonArrayKeyword(JsonArray enumValues) {
        super(enumValues);
    }

    public JsonArray getJsonArray() {
        return value();
    }
}
