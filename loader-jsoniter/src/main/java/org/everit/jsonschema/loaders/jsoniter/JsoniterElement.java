package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.any.Any;
import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.json.JsonElement;
import org.everit.json.UnexpectedValueException;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JsoniterElement implements JsonElement<Any> {

    protected final Any any;
    protected final JsoniterApi api;

    protected JsoniterElement(Any any) {
        this.any = checkNotNull(any);
        api = new JsoniterApi();
    }

    @Override
    public JsonSchemaType type() {
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
                throw new UnexpectedValueException("Unexpected jsoniter type: " + any.valueType());
        }
    }

    @Override
    public Any unbox() {
        return any;
    }

    @Override
    public Object raw() {
        return any.object();
    }
}
