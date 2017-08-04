package io.sbsp.jsonschema.loader;

import com.google.common.base.Preconditions;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.utils.JsonUtils;

import javax.json.JsonObject;

public class BaseLoaderTest {

    protected final JsonObject testsForType;
    protected final Schema e;

    public BaseLoaderTest(String resourceURL) {
        Preconditions.checkNotNull(resourceURL, "resourceURL must not be null");
        this.testsForType = JsonUtils.readResourceAsJson("/tests/" + resourceURL, JsonObject.class);
        e = JsonSchemaFactory.schemaFactory().load(this.testsForType);
    }

    protected JsonObject getJsonObjectForKey(String schemaName) {
        final JsonObject jsonObject = testsForType.getJsonObject(schemaName);
        Preconditions.checkArgument(jsonObject != null, "schema was null");
        return jsonObject;
    }

    protected Schema getSchemaForKey(String propertyKey) {
        final JsonObject jsonObjectForKey = getJsonObjectForKey(propertyKey);
        return JsonSchemaFactory.schemaFactory().load(jsonObjectForKey);
    }

    protected JsonObject readResource(String relativeURL) {
        return JsonUtils.readResourceAsJson("/tests/" + relativeURL, JsonObject.class);
    }
}
