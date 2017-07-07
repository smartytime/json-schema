package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import org.everit.json.JsonElement;
import org.everit.json.JsonObject;

import java.util.Optional;
import java.util.Set;

public class JsoniterObject extends JsoniterValue implements JsonObject<Any> {

    public JsoniterObject(Any any) {
        super(any);
    }

    @Override
    public Set<String> properties() {
        return any.keys();
    }

    @Override
    public Optional<JsonElement<?>> find(String key) {
        if(any.keys().contains(key)) {
            Any any = this.any.get(key);
            if (any.valueType() == ValueType.INVALID) {
                return Optional.empty();
            } else {
                return Optional.of(api.of(any));
            }
        }
        return Optional.empty();
    }
}
