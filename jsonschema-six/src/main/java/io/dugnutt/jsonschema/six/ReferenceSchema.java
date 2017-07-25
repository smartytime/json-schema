package io.dugnutt.jsonschema.six;

/**
 * This class is used to resolve JSON pointers.
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */
// @Builder
// @Getter
// @EqualsAndHashCode(exclude = "schemaInfo")
// public final class ReferenceSchema implements Schema {

    // @NotNull
    // private final JsonSchemaInfo schemaInfo;
    //
    // @NotNull
    // private final URI referenceURI;
    //
    // @NotNull
    // private final URI absoluteReferenceURI;
    //
    // @Nullable
    // private final JsonSchema referredSchema;
    //
    // public ReferenceSchema(JsonSchemaInfo schemaInfo, URI referenceURI, URI absoluteReferenceURI, SchemaFactory schemaFactory) {
    //     this.schemaInfo = schemaInfo;
    //     this.referenceURI = checkNotNull(referenceURI, "referenceURI cannot be null");
    //     final SchemaLocation currentLocation = schemaInfo.getLocation();
    //     this.absoluteReferenceURI = currentLocation.getResolutionScope().resolve(referenceURI);
    //
    //     if (schemaFactory != null) {
    //         this.referredSchema = schemaFactory.dereferenceSchema(currentLocation.getDocumentURI(), this);
    //     } else {
    //         referredSchema = null;
    //     }
    // }
    //
    //
    //
    // public Optional<Schema> getReferredSchema() {
    //     return Optional.ofNullable(referredSchema);
    // }
    //
    // @Override
    // public JsonSchemaGenerator toJson(JsonSchemaGenerator writer) {
    //     return writer.write($REF, referenceURI.toString());
    // }
// }
