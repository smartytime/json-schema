package io.dugnutt.jsonschema.six;

import javax.json.JsonObject;
import java.net.URI;

public interface SchemaFactory {
    Schema dereferenceSchema(URI currentDocumentURI, ReferenceSchema referenceSchema, JsonObject currentDocument);
}
