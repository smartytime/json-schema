package org.martysoft.jsonschema.loader;

import com.google.common.base.Preconditions;
import org.martysoft.jsonschema.utils.JsonUtils;
import org.martysoft.jsonschema.v6.Schema;

import javax.json.JsonObject;

public class BaseLoaderTest {

    protected final JsonObject testsForType;
    protected final Schema e;

    public BaseLoaderTest(String resourceURL) {
        Preconditions.checkNotNull(resourceURL, "resourceURL must not be null");
        this.testsForType = JsonUtils.readResource(resourceURL, JsonObject.class);
        e = SchemaFactory.schemaFactory().load(this.testsForType);
    }

    protected JsonObject getJsonObjectForKey(String schemaName) {
        return testsForType.getJsonObject(schemaName);
    }

    protected Schema getSchemaForKey(String propertyKey) {
        return SchemaFactory.schemaFactory().load(getJsonObjectForKey(propertyKey));
    }
}
