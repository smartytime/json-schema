package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import org.everit.json.JsonObject;
import org.everit.json.JsonPointer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JsoniterPointer implements JsonPointer {
    private List<String> path;
    private JsoniterApi api = new JsoniterApi();

    public JsoniterPointer(List<String> path) {
        this.path = path;
    }

    public JsoniterPointer(String path) {
        this.path = Collections.singletonList(path);
    }

    @Override
    public List<java.lang.String> path() {
        return path;
    }

    @Override
    public String toURIFragment() {
        return null;
    }

    @Override
    public Optional<JsonObject<?>> queryFrom(JsonObject<?> object) {
        Any source = (Any) object.unbox();
        Any any = source.get((Object[]) path.toArray());
        if (any.valueType() == ValueType.INVALID) {
            return Optional.empty();
        } else {
            return Optional.of(api.of(any).asObject());
        }
    }
}
