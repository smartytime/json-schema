package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.everit.json.*;
import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.JsonWriter;

import java.util.Map;

public class JsoniterApi implements JsonApi<Any> {

    // @Override
    // public boolean isNull(Object subject) {
    //     return subject == null || (subject instanceof Any && ((Any) subject).valueType() == ValueType.NULL);
    // }


    @Override
    public JsonSchemaType schemaType(Any any) {
        switch (any.valueType()) {
            case ARRAY:
                return JsonSchemaType.Array;
            case BOOLEAN:
                return JsonSchemaType.Boolean;
            case NULL:
                return JsonSchemaType.Null;
            case NUMBER:
                return JsonSchemaType.Number;
            case OBJECT:
                return JsonSchemaType.Object;
            case STRING:
                return JsonSchemaType.String;
            default:
                throw new UnexpectedValueException("Unable to determine type for " + any.valueType());
        }
    }

    @Override
    public JsoniterObject fromMap(Map<String, Object> map, JsonPath path) {
        return new JsoniterObject(Any.wrap(map), path);
    }

    @Override
    public JsonObject readJson(String jsonValue) {
        return new JsoniterObject(JsonIterator.deserialize(jsonValue), JsonPath.rootPath());
    }

    @Override
    public JsonWriter getWriter() {
        return new AnyWriter();
    }

    @Override
    public JsonValue<?> of(Any obj, JsonPath path) {
        Any any = (Any) obj;
        switch (any.valueType()) {
            case ARRAY:
                return new JsoniterArray(any, path);
            case BOOLEAN:
                return new JsoniterValue(any, path);
            case NULL:
                return new JsoniterValue(any, path);
            case NUMBER:
                return new JsoniterValue(any, path);
            case OBJECT:
                return new JsoniterObject(any, path);
            case STRING:
                return new JsoniterValue(any, path);
            default:
                throw new UnexpectedValueException("Unable to box " + obj);
        }
    }

    @Override
    public JsonPointer pointer(JsonPath path) {
        return new JsoniterPointer(path);
    }
}
