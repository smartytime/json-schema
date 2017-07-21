package io.dugnutt.jsonschema.six;

/**
 * An interface to support de-referencing $ref schemas.
 */
public interface ReferenceSchemaLoader {
    Schema loadReferenceSchema(ReferenceSchema referenceSchema);
}
