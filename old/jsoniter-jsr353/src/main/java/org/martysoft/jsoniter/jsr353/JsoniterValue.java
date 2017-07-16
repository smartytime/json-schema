package io.dugnutt.jsoniter.jsr353;

import com.jsoniter.any.Any;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

// import javax.json.JsonValue;

public class JsoniterValue implements javax.json.JsonValue {

    protected final Any wrapped;
    private final AtomicReference<ValueType> valueType = new AtomicReference<>();

    public JsoniterValue(Any wrapped) {
        this.wrapped = checkNotNull(wrapped);
    }

    @Override
    public javax.json.JsonValue.ValueType getValueType() {
        if (valueType.get() == null) {
            valueType.set(calculateValidType());
        }
        return valueType.get();
    }

    @Override
    public JsonObject asJsonObject() {
        return (JsonObject) this;
    }

    @Override
    public JsonArray asJsonArray() {
        return (JsonArray) this;
    }

    private javax.json.JsonValue.ValueType calculateValidType() {
        if (wrapped.valueType() == com.jsoniter.ValueType.INVALID) {
            throw new IllegalStateException("Invalid node found");
        } else if (wrapped.valueType() == com.jsoniter.ValueType.BOOLEAN) {
            if (wrapped.toBoolean()) {
                return javax.json.JsonValue.ValueType.TRUE;
            } else {
                return javax.json.JsonValue.ValueType.FALSE;
            }
        } else {
            return javax.json.JsonValue.ValueType.valueOf(wrapped.valueType().name());
        }
    }
}
