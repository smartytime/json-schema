package org.martysoft.jsoniter.jsr353;

import com.jsoniter.any.Any;

import javax.json.JsonString;

public class JsoniterString extends JsoniterValue implements JsonString {

    public JsoniterString(Any wrapped) {
        super(wrapped);
    }

    @Override
    public String getString() {
        return wrapped.toString();
    }

    @Override
    public CharSequence getChars() {
        return wrapped.toString();
    }
}
