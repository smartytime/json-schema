package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.any.Any;
import org.everit.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class JsoniterValue extends JsoniterElement implements JsonValue<Any> {

    protected List<String> path = new ArrayList<>();

    public JsoniterValue(Any any) {
        super(any);
    }

    public List<String> path() {
        return path;
    }

    @Override
    public JsoniterApi api() {
        return new JsoniterApi();
    }
}
