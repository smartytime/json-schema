package org.everit.jsonschema.api;

import org.everit.json.JsonApi;

import java.io.StringWriter;
import java.util.Objects;

/**
 * Superclass of all other schema validator classes of this package.
 */
public abstract class Schema {

    protected final String schemaLocation;
    private final String title;
    private final String description;
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
     * Describes the instance as a JSONObject to {@code writer}.
     * <p>
     * First it adds the {@code "title} , {@code "description"} and {@code "id"} properties then calls
     * {@link #describePropertiesTo(JsonWriter)}, which will add the subclass-specific properties.
     * <p>
     * It is used by {@link #toString()} to serialize the schema instance into its JSON representation.
     *
     * @param writer it will receive the schema description
     */
    public final JsonWriter describeTo(final JsonWriter writer) {
        writer.object();
        writer.ifPresent("title", title);
        writer.ifPresent("description", description);
        writer.ifPresent("id", id);
        describePropertiesTo(writer);
        writer.endObject();
        return writer;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, id);
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
                    Objects.equals(id, schema.id);
        } else {
            return false;
        }
    }

    public String toString(JsonWriter jsonWriter) {
        StringWriter w = new StringWriter();
        describeTo(jsonWriter);
        return w.getBuffer().toString();
    }

    /**
     * Performs the schema validation.
     *
     * @param subject the object to be validated
     * @throws ValidationError if the {@code subject} is invalid against this schema.
     */
    // public abstract void validate(final Object subject);

    // protected ValidationError failure(String message, String keyword) {
    //     return new ValidationError(this, message, keyword, schemaLocation);
    // }

    // protected ValidationError failure(Class<?> expectedType, Object actualValue) {
    //     return new ValidationError(this, expectedType, actualValue, "type", schemaLocation);
    // }

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
     * Subclasses are supposed to override this method to describe the subclass-specific attributes.
     * This method is called by {@link #describeTo(JsonWriter)} after adding the generic properties if
     * they are present ({@code id}, {@code title} and {@code description}). As a side effect,
     * overriding subclasses don't have to open and close the object with {@link JsonWriter#object()}
     * and {@link JsonWriter#endObject()}.
     *
     * @param writer it will receive the schema description
     */
    void describePropertiesTo(final JsonWriter writer) {

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

        private JsonApi jsonApi;

        public abstract S build();

        public Builder<S> description(final String description) {
            this.description = description;
            return this;
        }

        public Builder<S> id(final JsonApi jsonApi) {
            this.jsonApi = jsonApi;
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
