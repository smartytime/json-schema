package org.everit.json.schema.loader;

import org.everit.json.JsonApi;
import org.everit.json.JsonElement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LegacyJsonObjectWrapper implements org.everit.json.JsonObject<JsonObject> {
    private final JsonObject wrapped;
    private final LegacyJsonObjectApi legacyApi = new LegacyJsonObjectApi();

    public LegacyJsonObjectWrapper(JsonObject wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Set<String> properties() {
        return wrapped.keySet();
    }

    @Override
    public Object raw() {
        return wrapped.value();
    }

    @Override
    public JsonObject unbox() {
        return wrapped;
    }

    @Override
    public JsonApi<?> api() {
        return legacyApi;
    }

    @Override
    public List<String> path() {
        return null;
    }

    @Override
    public Optional<JsonElement<?>> find(String key) {
        return wrapped.maybe(key)
                .map(legacyApi::of);
    }
}
