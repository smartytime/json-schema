package io.dugnutt.jsonschema.six;

import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.validation.constraints.NotNull;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;

/**
 * Superclass of all other schema validator classes of this package.
 */
@Getter
public abstract class Schema<THIS extends Schema, BUILDER extends Schema.Builder<THIS, BUILDER>> {

    /**
     * {@see $ID}
     */
    @NotNull
    @NonNull
    protected final SchemaLocation location;

    /**
     * {@see TITLE}
     */
    @Nullable
    private final String title;

    /**
     * {@see DESCRIPTION}
     */
    @Nullable
    private final String description;

    /**
     * {@see ALL_OF}
     */
    private final CombinedSchema allOfSchema;

    /**
     * {@see ANY_OF}
     */
    private final CombinedSchema anyOfSchema;

    /**
     * {@see ONE_OF}
     */
    private final CombinedSchema oneOfSchema;

    /**
     * {@see NOT}
     */
    private final Schema notSchema;

    /**
     * {@see ENUM}
     */
    private final JsonArray enumValues;

    /**
     * {@see CONST}
     */
    private final JsonValue constValue;

    /**
     * Constructor.
     *
     * @param builder the builder containing the optional title, description and id attributes of the schema
     */
    protected Schema(final Builder<?, ?> builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.enumValues = builder.enumValues;
        this.constValue = builder.constValue;
        this.allOfSchema = builder.allOfSchema;
        this.anyOfSchema = builder.anyOfSchema;
        this.oneOfSchema = builder.oneOfSchema;
        this.notSchema = builder.notSchema;
        if (builder.location != null) {
            this.location = checkNotNull(builder.location, "builder.location must not be null");
        } else {
            checkNotNull(builder.id, "builder.id must not be null");
            this.location = SchemaLocation.schemaLocation(builder.id);
        }
    }

    /**
     * Returns a relative JSON-Pointer url fragment for targeting this schema within it's containing document.
     *
     * @return #/path/to/schema
     */
    public URI getDocumentLocalURI() {
        return location.getJsonPointerFragment();
    }

    /**
     * Returns a value specified by $id
     */
    public URI getId() {
        return location.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, allOfSchema, anyOfSchema, oneOfSchema, notSchema, enumValues, constValue);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Schema) {
            Schema schema = (Schema) o;
            return schema.canEqual(this) &&
                    Objects.equals(title, schema.title) &&
                    Objects.equals(description, schema.description) &&
                    Objects.equals(allOfSchema, schema.allOfSchema) &&
                    Objects.equals(anyOfSchema, schema.anyOfSchema) &&
                    Objects.equals(oneOfSchema, schema.oneOfSchema) &&
                    Objects.equals(notSchema, schema.notSchema) &&
                    Objects.equals(enumValues, schema.enumValues) &&
                    Objects.equals(constValue, schema.constValue);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        final JsonGenerator generator = JsonProvider.provider().createGenerator(writer);
        toJson(generator);
        generator.close();
        return writer.getBuffer().toString();
    }

    public final BUILDER toBuilder() {
        return internalToBuilder().title(title)
                .description(description)
                .enumValues(enumValues)
                .constValue(constValue)
                .allOfSchema(allOfSchema)
                .anyOfSchema(anyOfSchema)
                .oneOfSchema(oneOfSchema)
                .notSchema(notSchema)
                .location(location);
    }

    /**
     * Writes the instance to a provided {@link JsonGenerator}
     */
    public final void toJson(final JsonGenerator generator) {
        toJson(new JsonSchemaGenerator(generator));
    }

    public final THIS withLocation(SchemaLocation location) {
        //todo:ericm:! Implement
        return internalWithLocation(location)
                .build();
    }

    protected final Stream<Schema> streamSchemaWithLocation(Collection<Schema> schemas, SchemaLocation location) {
        return StreamUtils.safeStream(schemas).map(schema -> schema.withLocation(location));
    }

    /**
     * Each schema implementation must provide a completely cloned instance of itself with a new location.  This means that any
     * schema must also update the location for any of its child schemas.
     *
     * @param schemaLocation
     * @return
     */
    protected abstract BUILDER internalWithLocation(SchemaLocation schemaLocation);

    protected abstract BUILDER internalToBuilder();

    protected JsonSchemaGenerator toJson(final JsonSchemaGenerator writer) {
        writer.object();
        if (!"#".equals(location.getAbsoluteURI().toString())) {
            writer.optionalWrite(JsonSchemaKeyword.$ID, location.getId());
        }
        writer.optionalWrite(JsonSchemaKeyword.TITLE, title);
        writer.optionalWrite(JsonSchemaKeyword.DESCRIPTION, description);

        writer.optionalWrite(ENUM, enumValues);

        writer.optionalWrite(NOT, notSchema);
        writer.optionalWrite(CONST, constValue);
        writer.optionalWrite(ALL_OF, allOfSchema);
        writer.optionalWrite(ANY_OF, anyOfSchema);
        writer.optionalWrite(ONE_OF, oneOfSchema);

        writePropertiesToJson(writer);
        writer.endObject();
        return writer;
    }

    /**
     * Since we add state in subclasses, but want those subclasses to be non final, this allows us to
     * have equals methods that satisfy the equals contract.
     * <p>
     * http://www.artima.com/lejava/articles/equality.html
     *
     * @param other the subject of comparison
     * @return {@code true } if {@code this} can be equal to {@code other}
     */
    protected boolean canEqual(final Object other) {
        return (other instanceof Schema);
    }

    /**
     * Convenience method for writing properties to a generator.  Overridden by subclasses
     * for any schema-specific properties.
     */
    protected void writePropertiesToJson(final JsonSchemaGenerator writer) {

    }

    /**
     * Abstract builder class for the builder classes of {@code Schema} subclasses. This builder is
     * used to load the generic properties of all types of schemas like {@code title} or
     * {@code description}.
     *
     * @param <S> the type of the schema being built by the builder subclass.
     */
    public abstract static class Builder<S extends Schema, BUILDER extends Schema.Builder<S, BUILDER>> {

        private String title;
        private String description;
        private SchemaLocation location;
        private URI id;
        private JsonArray enumValues;
        private JsonValue constValue;
        private CombinedSchema allOfSchema;
        private CombinedSchema anyOfSchema;
        private CombinedSchema oneOfSchema;
        private Schema notSchema;

        public Builder() {
            this.location = SchemaLocation.anonymousRoot();
        }

        public BUILDER allOfSchemas(Stream<Schema> schema) {
            List<Schema> schemas = schema.collect(Collectors.toList());
            if (schemas.size() > 0) {
                this.allOfSchema = CombinedSchema.allOf(schemas)
                        .location(location.withChildPath(ALL_OF))
                        .build();
            }

            return self();
        }

        public BUILDER anyOfSchemas(Stream<Schema> schema) {
            List<Schema> schemas = schema.collect(Collectors.toList());
            if (schemas.size() > 0) {
                this.anyOfSchema = CombinedSchema.anyOf(schemas)
                        .location(location.withChildPath(ANY_OF))
                        .build();
            }

            return self();
        }

        public abstract S build();

        public BUILDER constValue(JsonValue constValue) {
            this.constValue = constValue;
            return self();
        }


        public BUILDER forEachSchema(List<Schema> schemas, Consumer<Schema> consumer) {
            checkNotNull(consumer, "consumer must not be null");
            if (schemas != null) {
                schemas.stream().forEach(consumer);
            }
            return self();
        }

        public BUILDER description(final String description) {
            this.description = description;
            return self();
        }

        public BUILDER enumValues(JsonArray enumValues) {
            this.enumValues = enumValues;
            return self();
        }

        public SchemaLocation getLocation() {
            return location;
        }

        public BUILDER location(String locationUri) {
            checkNotNull(locationUri, "locationUri must not be null");
            this.location = SchemaLocation.schemaLocation(locationUri);
            return self();
        }

        public BUILDER location(SchemaLocation location) {
            checkNotNull(location, "location must not be null");
            this.location = location;
            return self();
        }

        public BUILDER notSchema(Schema schema) {
            this.notSchema = schema;
            return self();
        }

        public BUILDER oneOfSchemas(Stream<Schema> schema) {
            List<Schema> schemas = schema.collect(Collectors.toList());
            if (schemas.size() > 0) {
                this.oneOfSchema = CombinedSchema.oneOf(schemas)
                        .location(location.withChildPath(ONE_OF))
                        .build();
            }
            return self();
        }

        public BUILDER optionalID(String id) {
            if (id != null) {
                this.location = SchemaLocation.schemaLocation(id);
            }
            return self();
        }

        public abstract BUILDER self();

        public BUILDER title(final String title) {
            this.title = title;
            return self();
        }

        protected BUILDER allOfSchema(CombinedSchema combinedSchema) {
            this.allOfSchema = combinedSchema;
            return self();
        }

        protected List<Schema> withLocation(List<Schema> schemas, SchemaLocation location) {
            return StreamUtils.safeStream(schemas).map(schema -> schema.withLocation(location)).collect(Collectors.toList());
        }

        protected BUILDER anyOfSchema(CombinedSchema combinedSchema) {
            this.anyOfSchema = combinedSchema;
            return self();
        }

        protected BUILDER oneOfSchema(CombinedSchema combinedSchema) {
            this.oneOfSchema = combinedSchema;
            return self();
        }
    }
}
