package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.six.enums.JsonSchemaType;
import io.dugnutt.jsonschema.six.keywords.ArrayKeywords;
import io.dugnutt.jsonschema.six.keywords.NumberKeywords;
import io.dugnutt.jsonschema.six.keywords.ObjectKeywords;
import io.dugnutt.jsonschema.six.keywords.StringKeywords;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.utils.JsonUtils.prettyPrintGeneratorFactory;

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
            while (schema instanceof ReferenceSchema) {
                schema = factory.loadRefSchema(schema, refURI, currentDocument);
                if (infiniteLoopPrevention++ > 10) {
                    throw new IllegalStateException("Too many nested references");
                }
            }
            this.refSchema = schema;
        } else {
            this.refSchema = null;
        }

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
    public JsonSchemaGenerator toJson(JsonSchemaGenerator writer) {
        return writer.object()
                .write($REF, refURI)
                .endObject();
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
    public Optional<JsonValue> getDefaultValue() {
        return requireRefSchema().getDefaultValue();
    }

    @Override
    public SchemaLocation getLocation() {
        return location;
    }

    @Override
    public ArrayKeywords getArrayKeywords() {
        return requireRefSchema().getArrayKeywords();
    }

    @Override
    public Optional<Schema> getNotSchema() {
        return requireRefSchema().getNotSchema();
    }

    @Override
    public Optional<JsonValue> getConstValue() {
        return requireRefSchema().getConstValue();
    }

    @Override
    public NumberKeywords getNumberKeywords() {
        return requireRefSchema().getNumberKeywords();
    }

    @Override
    public URI getId() {
        return requireRefSchema().getId();
    }

    @Override
    public String getTitle() {
        return requireRefSchema().getTitle();
    }

    @Override
    public String getDescription() {
        return requireRefSchema().getDescription();
    }

    @Override
    public List<Schema> getAllOfSchemas() {
        return requireRefSchema().getAllOfSchemas();
    }

    @Override
    public List<Schema> getAnyOfSchemas() {
        return requireRefSchema().getAnyOfSchemas();
    }

    @Override
    public List<Schema> getOneOfSchemas() {
        return requireRefSchema().getOneOfSchemas();
    }

    @Override
    public Set<JsonSchemaType> getTypes() {
        return requireRefSchema().getTypes();
    }

    @Override
    public Optional<JsonArray> getEnumValues() {
        return requireRefSchema().getEnumValues();
    }

    @Override
    public ObjectKeywords getObjectKeywords() {
        return requireRefSchema().getObjectKeywords();
    }

    @Override
    public StringKeywords getStringKeywords() {
        return requireRefSchema().getStringKeywords();
    }

    @Override
    public boolean hasStringKeywords() {
        return requireRefSchema().hasStringKeywords();
    }

    @Override
    public boolean hasObjectKeywords() {
        return requireRefSchema().hasObjectKeywords();
    }

    @Override
    public boolean hasArrayKeywords() {
        return requireRefSchema().hasArrayKeywords();
    }

    @Override
    public boolean hasNumberKeywords() {
        return requireRefSchema().hasNumberKeywords();
    }

    public static class ReferenceSchemaBuilder {

    }
}
