package io.dugnutt.jsoniter.jsr353;

import com.jsoniter.any.Any;
import io.dugnutt.json.BaseJsonArray;
import io.dugnutt.json.ImmutableJsonArray;

import javax.json.JsonValue;
import java.util.function.Function;

public class JsoniterArray extends BaseJsonArray<Any> implements ImmutableJsonArray {

    protected final Any wrapped;

    public JsoniterArray(Any wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isNull(int index) {
        com.jsoniter.ValueType jsoniterType = wrapped.get(index).valueType();
        return jsoniterType == com.jsoniter.ValueType.NULL || jsoniterType == com.jsoniter.ValueType.INVALID;
    }

    @Override
    public JsonValue jsonValueOf(Any next) {
        return JsoniterHelper.wrapAny(next);
    }

    @Override
    public ValueType jsonTypeOf(Any any) {
        return JsoniterHelper.typeofAny(any);
    }

    @Override
    public Function<Any, String> getString() {
        return JsoniterHelper.getStringFunction();
    }

    @Override
    public Function<Any, Number> getNumber() {
        return JsoniterHelper.getNumberFunction();
    }

    @Override
    public Function<Any, Boolean> getBoolean() {
        return JsoniterHelper.getBooleanFunction();
    }

    public Any fetch(Integer integer) {
        return wrapped.get((int) integer);
    }

    @Override
    public int size() {
        return wrapped.size();
    }
}
