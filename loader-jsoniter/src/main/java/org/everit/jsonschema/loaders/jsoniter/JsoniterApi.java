package org.everit.jsonschema.loaders.jsoniter;

import com.google.common.io.CharStreams;
import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import lombok.SneakyThrows;
import org.everit.json.JsonApi;
import org.everit.json.JsonElement;
import org.everit.json.JsonObject;
import org.everit.json.JsonPath;
import org.everit.json.JsonPointer;
import org.everit.json.JsonValue;
import org.everit.json.UnexpectedValueException;
import org.everit.jsonschema.api.JsonSchemaType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public class JsoniterApi implements JsonApi<Any> {

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
                Number number = (Number) any.object();
                if(number instanceof Long || number instanceof Integer || number instanceof Short) {
                    return JsonSchemaType.Integer;
                } else if(number instanceof Double || number instanceof Float || number instanceof BigDecimal) {
                    return JsonSchemaType.Number;
                } else {
                    throw new UnexpectedValueException("Unrecognized number type: " + number.getClass());
                }
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
    @SneakyThrows
    public JsonElement<?> readJson(InputStream stream, Charset charset) {
        String string = CharStreams.toString( new InputStreamReader( stream, charset ) );
        return of(JsonIterator.deserialize(string), JsonPath.rootPath());
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
    public Optional<JsonObject<?>> query(JsonObject<?> toBeQueried, JsonPointer pointer) {
        Any source = (Any) toBeQueried.unbox();
        JsonPath pointerPath = pointer.jsonPath();
        Any any = source.get((Object[]) pointerPath.toArray());
        if (any.valueType() == ValueType.INVALID) {
            return Optional.empty();
        } else {
            JsonValue<?> newValue = of(any, pointerPath);
            return Optional.of(newValue.asObject());
        }
    }
}
