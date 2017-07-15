package org.martysoft.jsoniter.jsr353;

import com.jsoniter.any.Any;

import javax.json.JsonException;
import javax.json.JsonValue;
import java.util.function.Function;

public class JsoniterHelper {

    public static Function<Any, Boolean> getBooleanFunction() {
        return Any::toBoolean;
    }

    public static Function<Any, Number> getNumberFunction() {
        return Any::toDouble;
    }

    public static Function<Any, String> getStringFunction() {
        return Any::toString;
    }

    public static JsonValue.ValueType typeofAny(Any any) {
        com.jsoniter.ValueType anyType = any.valueType();
        switch (anyType) {
            case ARRAY:
                return JsonValue.ValueType.ARRAY;
            case BOOLEAN:
                return any.toBoolean() ? JsonValue.ValueType.TRUE : JsonValue.ValueType.FALSE;
            case INVALID:
                throw new JsonException("Can't create JsonValue from invalid type");
            case NULL:
                return JsonValue.ValueType.NULL;
            case NUMBER:
                return JsonValue.ValueType.NUMBER;
            case OBJECT:
                return JsonValue.ValueType.OBJECT;
            case STRING:
                return JsonValue.ValueType.STRING;
            default:
                throw new JsonException("Unable to determine value type");
        }
    }

    public static JsonValue wrapAny(Any any) {
        com.jsoniter.ValueType anyType = any.valueType();
        switch (anyType) {
            case ARRAY:
                return new JsoniterArray(any);
            case BOOLEAN:
                return any.toBoolean() ? JsonValue.TRUE : JsonValue.FALSE;
            case INVALID:
                return null;
            case NULL:
                return JsonValue.NULL;
            case NUMBER:
                return new JsoniterNumber(any);
            case OBJECT:
                return new JsoniterObject(any);
            case STRING:
                return new JsoniterString(any);
            default:
                throw new JsonException("Unable to determine value type");
        }
    }
}
