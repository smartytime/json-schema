package io.dugnutt.jsonschema.six;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;

/**
 * This class is used to resolve JSON pointers.
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */

@EqualsAndHashCode
public final class ReferenceSchema implements Schema {

    /**
     * Contains a reference to the actual loaded schema.
     */
    private final AtomicReference<Schema> refSchema = new AtomicReference<>();

    @NonNull
    private final SchemaFactory factory;

    @NonNull
    private final SchemaBuildingContext buildingContext;

    @NonNull
    private final JsonSchemaInfo info;

    @NonNull
    private final URI refURI;

    @Nullable
    private final JsonObject currentDocument;

    @Builder(builderMethodName = "refSchemaBuilder")
    public ReferenceSchema(SchemaFactory factory, SchemaBuildingContext buildingContext, JsonSchemaInfo info, URI refURI, JsonObject currentDocument) {
        this.factory = factory;
        this.buildingContext = buildingContext;
        this.info = info;
        this.refURI = refURI;
        this.currentDocument = currentDocument;
    }

    public static ReferenceSchemaBuilder refSchemaBuilder(URI refURI) {
        return new ReferenceSchemaBuilder().refURI(refURI);
    }

    private Schema refSchema() {
        if(refSchema.get() == null) {
            synchronized (refSchema) {
                if (refSchema.get() == null) {
                    final URI documentURI = info.getContainedBy().getDocumentURI();
                    final URI absoluteReferenceURI = info.getLocation().getResolutionScope().resolve(refURI);
                    refSchema.set(factory.dereferenceSchema(buildingContext, documentURI, absoluteReferenceURI, currentDocument));
                }
            }
        }
        return refSchema.get();
    }

    public URI getRefURI() {
        return refURI;
    }

    @Override
    public JsonSchemaGenerator toJson(JsonSchemaGenerator writer) {
        return writer.write($REF, refURI);
    }

    @Override
    public SchemaLocation getLocation() {
        return info.getLocation();
    }

    @Override
    public Optional<ArrayKeywords> getArrayKeywords() {
        return refSchema().getArrayKeywords();
    }

    @Override
    public Optional<Schema> getNotSchema() {
        return refSchema().getNotSchema();
    }

    @Override
    public Optional<JsonValue> getConstValue() {
        return refSchema().getConstValue();
    }

    @Override
    public Optional<NumberKeywords> getNumberKeywords() {
        return refSchema().getNumberKeywords();
    }

    @Override
    public String getId() {
        return refSchema().getId();
    }

    @Override
    public String getTitle() {
        return refSchema().getTitle();
    }

    @Override
    public String getDescription() {
        return refSchema().getDescription();
    }

    @Override
    public List<Schema> getAllOfSchemas() {
        return refSchema().getAllOfSchemas();
    }

    @Override
    public List<Schema> getAnyOfSchemas() {
        return refSchema().getAnyOfSchemas();
    }

    @Override
    public List<Schema> getOneOfSchemas() {
        return refSchema().getOneOfSchemas();
    }

    @Override
    public Set<JsonSchemaType> getTypes() {
        return refSchema().getTypes();
    }

    @Override
    public Optional<JsonArray> getEnumValues() {
        return refSchema().getEnumValues();
    }

    @Override
    public Optional<ObjectKeywords> getObjectKeywords() {
        return refSchema().getObjectKeywords();
    }

    @Override
    public Optional<StringKeywords> getStringKeywords() {
        return refSchema().getStringKeywords();
    }

    @Override
    public boolean hasStringKeywords() {
        return refSchema().hasStringKeywords();
    }

    @Override
    public boolean hasObjectKeywords() {
        return refSchema().hasObjectKeywords();
    }

    @Override
    public boolean hasArrayKeywords() {
        return refSchema().hasArrayKeywords();
    }

    @Override
    public boolean hasNumberKeywords() {
        return refSchema().hasNumberKeywords();
    }

    public static class ReferenceSchemaBuilder {

    }
}
