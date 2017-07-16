package io.dugnutt.jsonschema.six;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

import javax.annotation.Nullable;
import javax.json.stream.JsonGenerator;
import javax.validation.constraints.NotBlank;

/**
 * Superclass of all other schema validator classes of this package.
 */
@Getter
@EqualsAndHashCode
public abstract class Schema {

    @URL
    protected final String schemaLocation;

    @Nullable
    private final String title;

    @Nullable
    private final String description;

    @NotBlank
    private final String id;

    /**
     * Constructor.
     *
     * @param builder the builder containing the optional title, description and id attributes of the schema
     */
    protected Schema(final Builder<?> builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.id = builder.id;
        this.schemaLocation = builder.schemaLocation;
    }

    /**
     * Determines if this {@code Schema} instance defines any restrictions for the object property
     * denoted by {@code field}. The {@code field} should be a JSON pointer, denoting the property to
     * be queried.
     * <p>
     * For example the field {@code "#/rectangle/a"} is defined by the following schema:
     * <p>
     * <pre>
     * <code>
     * objectWithSchemaRectangleDep" : {
     *   "type" : "object",
     *   "dependencies" : {
     *       "d" : {
     *           "type" : "object",
     *           "properties" : {
     *               "rectangle" : {"$ref" : "#/definitions/Rectangle" }
     *           }
     *       }
     *   },
     *   "definitions" : {
     *       "size" : {
     *           "type" : "number",
     *           "minimum" : 0
     *       },
     *       "Rectangle" : {
     *           "type" : "object",
     *           "properties" : {
     *               "a" : {"$ref" : "#/definitions/size"},
     *               "b" : {"$ref" : "#/definitions/size"}
     *           }
     *       }
     *    }
     * }
     * </code>
     * </pre>
     * <p>
     * The default implementation of this method always returns false.
     *
     * @param field should be a JSON pointer in its string representation.
     * @return {@code true} if the propertty denoted by {@code field} is defined by this schema
     * instance
     */
    public boolean definesProperty(final String field) {
        return false;
    }

    /**
     * Writes the instance to a provided {@link JsonGenerator}
     */
    public final void toJson(final JsonGenerator generator) {
        toJson(new JsonSchemaGenerator(generator));
    }

    protected final JsonSchemaGenerator toJson(final JsonSchemaGenerator writer) {
        writer.object();
        writer.optionalWrite(JsonSchemaProperty.TITLE, title);
        writer.optionalWrite(JsonSchemaProperty.DESCRIPTION, description);
        writer.optionalWrite(JsonSchemaProperty.ID, id);
        propertiesToJson(writer);
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
    protected void propertiesToJson(final JsonSchemaGenerator writer) {

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
        private String id;
        private String schemaLocation;

        public abstract S build();

        public Builder<S> description(final String description) {
            this.description = description;
            return this;
        }

        public Builder<S> id(final String id) {
            this.id = id;
            return this;
        }

        public Builder<S> schemaLocation(String schemaLocation) {
            this.schemaLocation = schemaLocation;
            return this;
        }

        public Builder<S> title(final String title) {
            this.title = title;
            return this;
        }
    }
}
