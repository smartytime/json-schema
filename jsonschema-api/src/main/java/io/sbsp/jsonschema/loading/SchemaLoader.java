package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Optional;

/**
 * If you're trying to load a schema from an input source, you want {@link SchemaReader}.
 * <p>
 * This interface is used within the different processing elements.  Instead of working on raw {@link JsonObject}
 * instances, it works on {@link JsonValueWithPath} which means that the source document or fragment is already
 * being traversed.
 */
public interface SchemaLoader {

    /**
     * Loads all the values of a schema into a builder.  This builder can then be modified before calling build()
     *
     * @param forSchema     The source json, path-aware
     * @param loadingReport A place to log loading errors
     * @return A schema builder instance.
     */
    default SchemaBuilder schemaBuilder(JsonValueWithPath forSchema, LoadingReport loadingReport) {
        return subSchemaBuilder(forSchema, forSchema, loadingReport);
    }

    /**
     * Loads a $ref schema
     * @param referencedFrom The schema where the $ref resides
     * @param refURI The URI to the $ref
     * @param currentDocument The current document being processed (from which {@code #referencedFrom} was loaded)
     * @param report A place to log loading errors
     * @return A loaded ref schema
     */
    Schema loadRefSchema(Schema referencedFrom, URI refURI, JsonObject currentDocument, LoadingReport report);

    /**
     * Loads a subschema within a document
     * @param schemaJson The path-aware json object representing the subschema
     * @param inDocument The document the schema resides in
     * @param loadingReport A place to log errors
     * @return The loaded schema
     */
    Schema loadSubSchema(JsonValueWithPath schemaJson, JsonObject inDocument, LoadingReport loadingReport);

    /**
     * Loads all the values of a subschema into a builder.  This builder can then be modified before calling build()
     * @param schemaJson The source json, path-aware
     * @param inDocument The document containing hte subschema
     * @param loadingReport A place to log loading errors
     * @return A schema builder instance.
     */
    SchemaBuilder subSchemaBuilder(JsonValueWithPath schemaJson, JsonObject inDocument, LoadingReport loadingReport);

    /**
     * Looks for a schema that's already been loaded by this loader.
     *
     * @param schemaLocation The absolute URI for the schema
     * @return A schema, if one has been loaded
     */
    Optional<Schema> findLoadedSchema(URI schemaLocation);

    /**
     * Registers a schema that's been loaded
     *
     * @param schema The schema to register
     */
    void registerLoadedSchema(Schema schema);
}
