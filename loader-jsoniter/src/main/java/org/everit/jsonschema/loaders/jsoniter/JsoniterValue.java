package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.any.Any;
import org.everit.json.JsonPath;
import org.everit.json.JsonValue;

public class JsoniterValue extends JsoniterElement implements JsonValue<Any> {

    protected JsoniterValue(Any wrapped, JsonPath path) {
        super(wrapped, path);
    }
}
