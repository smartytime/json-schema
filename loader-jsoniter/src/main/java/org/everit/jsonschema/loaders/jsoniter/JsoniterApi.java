package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.everit.jsonschema.api.JsonSchemaWriter;
import org.everit.json.JsonApi;
import org.everit.json.JsonObject;
import org.everit.json.JsonPointer;
import org.everit.json.JsonValue;
import org.everit.json.UnexpectedValueException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsoniterApi implements JsonApi<Any> {

    // @Override
    // public boolean isNull(Object subject) {
    //     return subject == null || (subject instanceof Any && ((Any) subject).valueType() == ValueType.NULL);
    // }

    @Override
    public JsonObject fromMap(Map<String, Object> map) {
        return new JsoniterObject(Any.wrap(map));
    }

    @Override
    public JsonObject readJson(String jsonValue) {
        return new JsoniterObject(JsonIterator.deserialize(jsonValue));
    }

    @Override
    public JsonSchemaWriter getWriter() {
        return new AnyWriter();
    }

    @Override
    public JsonValue<?> of(Any obj) {
        Any any = (Any) obj;
        switch (any.valueType()) {
            case ARRAY:
                return new JsoniterArray(any);
            case BOOLEAN:
                return new JsoniterValue(any);
            case NULL:
                return new JsoniterValue(any);
            case NUMBER:
                return new JsoniterValue(any);
            case OBJECT:
                return new JsoniterObject(any);
            case STRING:
                return new JsoniterValue(any);
            default:
                throw new UnexpectedValueException("Unable to box " + obj);
        }
    }

    @Override
    public JsonPointer pointer(List<String> path) {
        return new JsoniterPointer(path);
    }

    @Override
    public JsonPointer pointer(String... path) {
        return new JsoniterPointer(Arrays.asList(path));
    }

    @Override
    public void handleException(Exception e) {
        throw new IllegalStateException("Something bad happened");

    }
}
