package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.any.Any;
import org.everit.json.BaseJsonElement;
import org.everit.json.JsonPath;

public abstract class JsoniterElement extends BaseJsonElement<Any, JsoniterApi> {

    protected JsoniterElement(Any wrapped, JsonPath path) {
        super(wrapped, path, new JsoniterApi());
    }

    @Override
    public Object raw() {
        return wrapped.object();
    }
}
