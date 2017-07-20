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
import java.util.List;
import java.util.Objects;
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
public abstract class Schema {

    @NotNull
    @NonNull
    protected final SchemaLocation location;

    @Nullable
    private final String title;

    @Nullable
    private final String description;

    private final CombinedSchema allOfSchema;
    private final CombinedSchema anyOfSchema;
    private final CombinedSchema oneOfSchema;
    private final Schema notSchema;
    private final JsonArray enumValues;
    private final JsonValue constValue;

    /**
     * Constructor.
     *
     * @param builder the builder containing the optional title, description and id attributes of the schema
     */
    protected Schema(final Builder<?> builder) {
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

    public URI getDocumentLocalURI() {
        return location.getJsonPointerFragment();
    }

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

    /**
     * Writes the instance to a provided {@link JsonGenerator}
     */
    public final void toJson(final JsonGenerator generator) {
        toJson(new JsonSchemaGenerator(generator));
    }

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
    public abstract static class Builder<S extends Schema> {

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
        public Builder(String id) {
            this.id = URI.create(id);
            this.location = SchemaLocation.schemaLocation(id);
        }
        public Builder(SchemaLocation location) {
            checkNotNull(location, "location must not be null");
            this.location = location;
        }

        public Builder<S> allOfSchemas(Stream<Schema> schema) {
            List<Schema> schemas = schema.collect(Collectors.toList());
            if (schemas.size() > 0) {
                this.allOfSchema = CombinedSchema.allOf(location.withChildPath(ALL_OF), schemas).build();
            }

            return this;
        }

        public Builder<S> anyOfSchemas(Stream<Schema> schema) {
            List<Schema> schemas = schema.collect(Collectors.toList());
            if (schemas.size() > 0) {
                this.anyOfSchema = CombinedSchema.anyOf(location.withChildPath(ANY_OF), schemas)
                        .build();
            }

            return this;
        }

        public abstract S build();

        public Builder<S> constValue(JsonValue constValue) {
            this.constValue = constValue;
            return this;
        }

        public Builder<S> description(final String description) {
            this.description = description;
            return this;
        }

        public Builder<S> enumValues(JsonArray enumValues) {
            this.enumValues = enumValues;
            return this;
        }

        public SchemaLocation getLocation() {
            return location;
        }

        public Builder<S> location(String locationUri) {
            checkNotNull(locationUri, "locationUri must not be null");
            this.location = SchemaLocation.schemaLocation(locationUri);
            return this;
        }

        public Builder<S> location(SchemaLocation location) {
            checkNotNull(location, "location must not be null");
            this.location = location;
            return this;
        }

        public Builder<S> notSchema(Schema schema) {
            this.notSchema = schema;
            return this;
        }

        public Builder<S> oneOfSchemas(Stream<Schema> schema) {
            List<Schema> schemas = schema.collect(Collectors.toList());
            if (schemas.size() > 0) {
                this.oneOfSchema = CombinedSchema.oneOf(location.withChildPath(ONE_OF), schemas).build();
            }
            return this;
        }

        public Builder<S> optionalID(String id) {
            if (id != null) {
                this.location = SchemaLocation.schemaLocation(id);
            }
            return this;
        }

        public Builder<S> title(final String title) {
            this.title = title;
            return this;
        }
    }
}
