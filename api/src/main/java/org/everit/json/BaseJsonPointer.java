package org.everit.json;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseJsonPointer implements JsonPointer {
    protected final JsonPath jsonPath;

    public BaseJsonPointer(JsonPath jsonPath) {
        this.jsonPath = checkNotNull(jsonPath);
    }

    @Override
    public JsonPath jsonPath() {
        return jsonPath;
    }

    /**
     * todo:ericm Implement
     * @return
     */
    @Override
    public String toURIFragment() {
        return null;
    }
}
