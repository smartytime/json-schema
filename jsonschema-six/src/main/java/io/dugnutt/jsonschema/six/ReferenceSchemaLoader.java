package io.dugnutt.jsonschema.six;

import javax.json.JsonObject;

public interface ReferenceSchemaLoader {
    Schema loadReferenceSchema(ReferenceSchema referenceSchema);
    Schema loadLocalReferenceSchema(ReferenceSchema referenceSchema, JsonObject rootSchemaJson);
}
