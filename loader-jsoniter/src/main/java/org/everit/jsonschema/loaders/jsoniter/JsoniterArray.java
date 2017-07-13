package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.any.Any;
import org.everit.json.JsonArray;
import org.everit.json.JsonElement;
import org.everit.json.JsonPath;

import java.util.ArrayList;
import java.util.List;

public class JsoniterArray extends JsoniterValue implements JsonArray<Any> {

    protected JsoniterArray(Any wrapped, JsonPath jsonPath) {
        super(wrapped, jsonPath);
    }

    @Override
    public int length() {
        return wrapped.size();
    }

    @Override
    public List<JsonElement<?>> toList() {
        List<JsonElement<?>> list = new ArrayList<>();
        for (int i = 0; i < wrapped.size(); i++) {
            Any any = wrapped.get(i);
            list.add(jsonApi.of(any, path.child(i)));
        }
        return list;
    }

    @Override
    public JsonElement<?> get(int i) {
        return jsonApi.of(wrapped.get(i), path.child(i));
    }
}
