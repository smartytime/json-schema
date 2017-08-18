package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public interface SchemaLoader {

    Schema loadSchema(JsonValueWithPath schemaJson, LoadingReport loadingReport);
    Schema loadRefSchema(Schema referencedFrom, URI refURI, JsonObject currentDocument, LoadingReport report);

    Schema loadSubSchema(JsonValueWithPath schemaJson, JsonObject inDocument, LoadingReport loadingReport);
    Optional<Schema> findLoadedSchema(URI schemaLocation);
    void registerLoadedSchema(Schema schema);
    void registerLoadedDocument(URI documentURI, JsonObject document);

    SchemaBuilder subSchemaBuilder(JsonValueWithPath schemaJson, JsonObject inDocument, LoadingReport loadingReport);

    SchemaBuilder schemaBuilder();
    SchemaBuilder schemaBuilder(URI $id);

    default SchemaBuilder schemaBuilder(JsonValueWithPath forSchema, LoadingReport loadingReport) {
        return subSchemaBuilder(forSchema, forSchema, loadingReport);
    }

    default SchemaBuilder schemaBuilder(String $id) {
        checkNotNull($id, "$id must not be null");
        return schemaBuilder(URI.create($id));
    }

    JsonProvider getProvider();

}
