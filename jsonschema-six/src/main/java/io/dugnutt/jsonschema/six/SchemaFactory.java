package io.dugnutt.jsonschema.six;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Optional;

public interface SchemaFactory {
    Schema loadRefSchema(Schema referencedFrom, URI refURI, JsonObject currentDocument);

    Optional<Schema> findCachedSchema(URI schemaURI);
}
