package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ReferenceSchema;

/**
 * Top-level interface for fetching and loading reference schemas.  Any implementation might have different configuration
 * options for pre-caching schemas, http configuration, etc.  Either way, this interface is the main entry point for the
 * application to load reference schemas.
 */
public interface ReferenceSchemaFactory {

    /**
     * Retrieves a reference or pointer schema for the given loader model.
     *
     * @param forModel Represents the location in the schema that we're trying to load a reference for
     * @return
     */
    ReferenceSchema.Builder createReferenceSchemaBuilder(SchemaLoadingContext forModel);


}
