package io.sbsp.jsonschema;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaFactory;
import io.sbsp.jsonschema.loading.SchemaLoader;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.utils.JsonUtils.prettyPrintGeneratorFactory;

/**
 * This class is used to resolve JSON pointers.
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */

@EqualsAndHashCode(of = "refURI")
public abstract class RefSchema implements Schema {

    /**
     * Contains a reference to the actual loaded schema.
     */
    private final Schema refSchema;

    @NonNull
    private final SchemaLocation location;

    @NonNull
    private final URI refURI;

    public RefSchema(SchemaLoader factory, SchemaLocation location, URI refURI, JsonObject currentDocument, LoadingReport report) {
        checkNotNull(report, "report must not be null");
        this.location = location;
        this.refURI = refURI;

        int infiniteLoopPrevention = 0;
        if (factory != null) {
            Schema schema = this;
            URI thisRefURI = this.getRefURI();

            while (schema instanceof RefSchema) {
                schema = factory.loadRefSchema(schema, thisRefURI, currentDocument, report);
                if (schema instanceof RefSchema) {
                    thisRefURI = ((RefSchema) schema).getRefURI();
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

    protected RefSchema(SchemaLocation location, URI refURI, Schema refSchema) {
        checkNotNull(location, "location must not be null");
        checkNotNull(refSchema, "refSchema must not be null");
        checkNotNull(refURI, "refURI must not be null");
        this.location = location;
        this.refURI = refURI;
        this.refSchema = refSchema;
    }

    @Override
    public Map<KeywordInfo<?>, SchemaKeyword> getKeywords() {
        return requireRefSchema().getKeywords();
    }

    public Schema requireRefSchema() {
        checkNotNull(refSchema, "refSchema must not be null");
        return refSchema;
    }

    public URI getRefURI() {
        return refURI;
    }

    public Schema getRefSchema() {
        return refSchema;
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
    public abstract JsonGenerator toJson(JsonGenerator writer, JsonSchemaVersion version);

    @Override
    public abstract Draft6Schema asDraft6();

    @Override
    public abstract Draft4Schema asDraft4();

    @Override
    public abstract Draft3Schema asDraft3();
}
