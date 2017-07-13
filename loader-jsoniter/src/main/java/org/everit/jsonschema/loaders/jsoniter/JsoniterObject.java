package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import org.everit.json.JsonElement;
import org.everit.json.JsonObject;
import org.everit.json.JsonPath;

import java.util.Optional;
import java.util.Set;

public class JsoniterObject extends JsoniterValue implements JsonObject<Any> {

    public JsoniterObject(Any wrapped, JsonPath path) {
        super(wrapped, path);
    }

    @Override
    public Set<String> properties() {
        return wrapped.keys();
    }

    @Override
    public Optional<JsonElement<?>> find(String key) {
        if(wrapped.keys().contains(key)) {
            Any any = this.wrapped.get(key);
            if (any.valueType() == ValueType.INVALID) {
                return Optional.empty();
            } else {
                return Optional.of(jsonApi.of(any, path));
            }
        }
        return Optional.empty();
    }
}
