package io.dugnutt.jsoniter.jsr353;

import com.jsoniter.any.Any;
import io.dugnutt.json.BaseJsonObject;
import io.dugnutt.json.ImmutableJsonObject;

import javax.json.JsonValue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsoniterObject extends BaseJsonObject<Any> implements ImmutableJsonObject {

    private final Any wrapped;

    public JsoniterObject(Any wrapped) {
        this.wrapped = checkNotNull(wrapped);
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public Set<String> keySet() {
        return wrapped.keys();
    }

    @Override
    public JsonValue jsonValueOf(Any next) {
        return JsoniterHelper.wrapAny(next);
    }

    @Override
    protected Stream<String> cacheKeyStream() {
        return wrapped.keys().stream();
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

    @Override
    protected Any fetch(String s) {
        return wrapped.get(s);
    }
}
