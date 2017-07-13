package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import org.everit.json.BaseJsonPointer;
import org.everit.json.JsonObject;
import org.everit.json.JsonPath;

import java.util.Optional;

public class JsoniterPointer extends BaseJsonPointer {
    private JsoniterApi api = new JsoniterApi();

    public JsoniterPointer(JsonPath jsonPath) {
        super(jsonPath);
    }

    @Override
    public Optional<JsonObject<?>> queryFrom(JsonObject<?> object) {
        Any source = (Any) object.unbox();
        Any any = source.get((Object[]) jsonPath.toArray());
        if (any.valueType() == ValueType.INVALID) {
            return Optional.empty();
        } else {
            return Optional.of(api.of(any, jsonPath).asObject());
        }
    }
}
