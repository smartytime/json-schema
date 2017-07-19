package io.dugnutt.jsonschema.six;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Optional;

public interface SchemaFactory {

    Schema dereferenceRemoteSchema(ReferenceSchema referenceSchema);

    Schema dereferenceLocalSchema(ReferenceSchema referenceSchema, JsonPath localPath, JsonObject rootSchemaJson);

    Optional<JsonPath> resolveURILocally(URI documentURI, URI encounteredURI, JsonObject document);
}
