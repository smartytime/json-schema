package io.dugnutt.jsonschema.loader.reference;

import io.dugnutt.jsonschema.six.ReferenceSchemaLoader;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;
import lombok.Getter;
import lombok.experimental.Wither;

@Wither
@Getter
public class DefaultReferenceLoader implements ReferenceSchemaLoader {

    @Wither
    private SchemaCache schemaCache;
    private SchemaClient httpClient;

    public DefaultReferenceLoader(SchemaCache schemaCache, SchemaClient httpClient) {
        this.schemaCache = null;
        this.httpClient = new DefaultSchemaClient();
    }

    @Override
    public Schema loadReferenceSchema(ReferenceSchema referenceSchema) {
        // schemaCa che.cacheSchema(referenceSchema.getreferenceSchema.getSchemaLocation(), referenceSchema);
        //Look in cache

        //httpClient
        // httpClient.get()
        return null;

    }
}
