package io.sbsp.jsonschema;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.utils.JsonUtils.prettyPrintGeneratorFactory;

/**
 * This class is used to resolve JSON pointers.
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */

@EqualsAndHashCode(of = "refURI")
public final class ReferenceSchema implements Schema {

    /**
     * Contains a reference to the actual loaded schema.
     */
    private final Schema refSchema;

    @NonNull
    private final SchemaLocation location;

    @NonNull
    private final URI refURI;

    @Builder(builderMethodName = "refSchemaBuilder")
    public ReferenceSchema(SchemaFactory factory, SchemaLocation location, URI refURI, JsonObject currentDocument) {
        this.location = location;
        this.refURI = refURI;

        int infiniteLoopPrevention = 0;
        if (factory != null) {
            Schema schema = this;
            URI thisRefURI = this.getRefURI();

            while (schema instanceof ReferenceSchema) {
                schema = factory.loadRefSchema(schema, thisRefURI, currentDocument);
                if (schema instanceof ReferenceSchema) {
                    thisRefURI = ((ReferenceSchema) schema).getRefURI();
                }
                if (infiniteLoopPrevention++ > 10) {
                    throw new IllegalStateException("Too many nested references");
                }
            }
            this.refSchema = schema;
        } else {
            this.refSchema = null;
        }
    }

    @Override
    public Map<KeywordMetadata<?>, SchemaKeyword> getKeywords() {
        return requireRefSchema().getKeywords();
    }

    public static ReferenceSchemaBuilder refSchemaBuilder(URI refURI) {
        return new ReferenceSchemaBuilder().refURI(refURI);
    }

    public Optional<Schema> getRefSchema() {
        return Optional.ofNullable(refSchema);
    }

    public Schema requireRefSchema() {
        checkNotNull(refSchema, "refSchema must not be null");
        return refSchema;
    }

    public URI getRefURI() {
        return refURI;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean pretty) {
        final StringWriter stringWriter = new StringWriter();
        final JsonGenerator generator;
        if (pretty) {
            generator = prettyPrintGeneratorFactory().createGenerator(stringWriter);
        } else {
            generator = JsonProvider.provider().createGenerator(stringWriter);
        }
        this.toJson(new JsonSchemaGenerator(generator));
        generator.flush();
        return stringWriter.toString();
    }

    @Override
    public SchemaLocation getLocation() {
        return location;
    }

    @Override
    public URI getId() {
        return refSchema.getId();
    }

    @Override
    public URI getSchemaURI() {
        return refSchema.getSchemaURI();
    }

    @Override
    public String getTitle() {
        return refSchema.getTitle();
    }

    @Override
    public String getDescription() {
        return refSchema.getDescription();
    }

    @Override
    public JsonSchemaVersion getVersion() {
        return refSchema.getVersion();
    }

    @Override
    public JsonGenerator toJson(JsonGenerator writer, JsonSchemaVersion version) {
        return writer.writeStartObject()
                .write(SchemaKeyword.$ref.getKey(), getRefURI().toString())
                .writeEnd();
    }

    @Override
    public Draft6Schema asDraft6() {
        return refSchema.asDraft6();
    }

    @Override
    public Draft3Schema asDraft3() {
        return refSchema.asDraft3();
    }

    @Override
    public Draft4Schema asDraft4() {
        return refSchema.asDraft4();
    }

    public static class ReferenceSchemaBuilder {

    }
}
