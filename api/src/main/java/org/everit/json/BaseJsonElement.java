package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseJsonElement<X, A extends JsonApi<X>> implements JsonElement<X> {

    protected final X wrapped;
    protected final JsonPath path;
    protected final A jsonApi;
    protected JsonSchemaType schemaType;

    protected BaseJsonElement(X wrapped, JsonPath path, A jsonApi) {
        this.wrapped = checkNotNull(wrapped);
        this.path = checkNotNull(path);
        this.jsonApi = checkNotNull(jsonApi);
        schemaType = jsonApi.schemaType(wrapped);
        checkNotNull(schemaType, "type must not be null");
    }

    @Override
    public JsonPath path() {
        return path;
    }

    @Override
    public X unbox() {
        return wrapped;
    }

    @Override
    public JsonSchemaType schemaType() {
        return schemaType;
    }
}

