package io.sbsp.jsonschema.loading;

import com.google.common.base.Preconditions;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.ResourceLoader;
import io.sbsp.jsonschema.Schema;

import javax.json.JsonObject;

import static io.sbsp.jsonschema.ResourceLoader.resourceLoader;

public class BaseLoaderTest {

    protected final JsonObject testsForType;
    protected final Schema e;

    public BaseLoaderTest(String resourceURL) {
        Preconditions.checkNotNull(resourceURL, "resourceURL must not be null");
        this.testsForType = resourceLoader().readJsonObject("loading/" + resourceURL);
        e = SchemaLoaderImpl.schemaLoader().readSchema(this.testsForType);
    }

    protected JsonObject getJsonObjectForKey(String schemaName) {
        final JsonObject jsonObject = testsForType.getJsonObject(schemaName);
        Preconditions.checkArgument(jsonObject != null, "schema was null");
        return jsonObject;
    }

    protected Draft6Schema getSchemaForKey(String propertyKey) {
        final JsonObject jsonObjectForKey = getJsonObjectForKey(propertyKey);
        return SchemaLoaderImpl.schemaLoader().readSchema(jsonObjectForKey).asDraft6();
    }

    protected JsonObject readResource(String relativeURL) {
        return ResourceLoader.resourceLoader().readJsonObject(relativeURL);
    }
}
