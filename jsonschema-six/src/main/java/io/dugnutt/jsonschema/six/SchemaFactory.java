package io.dugnutt.jsonschema.six;

import javax.json.JsonObject;
import java.net.URI;

public interface SchemaFactory {
    Schema dereferenceSchema(SchemaBuildingContext context, URI currentDocumentURI, URI refURI, JsonObject currentDocument);
}
