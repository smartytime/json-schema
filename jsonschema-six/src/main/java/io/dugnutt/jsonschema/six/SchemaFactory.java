package io.dugnutt.jsonschema.six;

import javax.json.JsonObject;
import java.net.URI;

public interface SchemaFactory {
    JsonSchema dereferenceSchema(URI currentDocumentURI, URI refURI, JsonSchema referenceSchema, JsonObject currentDocument);
}
