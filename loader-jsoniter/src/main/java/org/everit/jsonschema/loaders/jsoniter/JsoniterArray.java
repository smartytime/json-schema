package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.any.Any;
import org.everit.json.JsonArray;
import org.everit.json.JsonElement;

import java.util.List;
import java.util.stream.Collectors;

public class JsoniterArray extends JsoniterValue implements JsonArray<Any> {
    public JsoniterArray(Any any) {
        super(any);
    }

    @Override
    public JsonElement<?> get(int i) {
        return api.of(any.get(i));
    }

    @Override
    public int length() {
        return any.size();
    }

    @Override
    public List<JsonElement<?>> toList() {
        return any.asList().stream().map(api::of).collect(Collectors.toList());
    }
}
