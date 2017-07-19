package io.dugnutt.jsonschema.six;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static java.util.Objects.requireNonNull;

/**
 * This class is used to resolve JSON pointers.
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */
public class ReferenceSchema extends Schema {

    @NotNull
    private final URI referenceURI;

    @NotNull
    private final URI absoluteReferenceURI;

    @Nullable
    private final Schema referredSchema;

    public ReferenceSchema(final Builder builder) {
        super(builder);
        this.referenceURI = URI.create(requireNonNull(builder.refValue, "refValue cannot be null"));
        this.absoluteReferenceURI = builder.getLocation().getDocumentUri().resolve(referenceURI);
        if (builder.referenceSchemaLoader != null) {
            this.referredSchema = checkNotNull(builder.referenceSchemaLoader.loadReferenceSchema(this));
        } else {
            this.referredSchema = null;
        }
    }

    public URI getReferenceURI() {
        return referenceURI;
    }

    public URI getAbsoluteReferenceURI() {
        return referenceURI;
    }

    public static Builder builder(SchemaLocation location) {
        return new Builder(location);
    }

    @Override
    public boolean definesProperty(String field) {
        // if (referredSchema == null) {
        //     throw new IllegalStateException("referredSchema must be injected before validation");
        // }
        // return referredSchema.definesProperty(field);
        //todo:ericm Revisit
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), referredSchema, referenceURI);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ReferenceSchema) {
            ReferenceSchema that = (ReferenceSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(referenceURI, that.referenceURI) &&
                    Objects.equals(referredSchema, that.referredSchema) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ReferenceSchema;
    }

    @Override
    protected void writePropertiesToJson(JsonSchemaGenerator writer) {
        writer.write($REF, referenceURI.toString());
    }

    public Optional<Schema> getReferredSchema() {
        return Optional.ofNullable(referredSchema);
    }

    /**
     * Called by a loader to set the referred root
     * schema after completing the loading process of the entire schema document.
     *
     * @param referredSchema the referred schema
     */
    // public void setReferredSchema(final Schema referredSchema) {
    // if (this.referredSchema != null) {
    //     throw new IllegalStateException("referredSchema can be injected only once");
    // }
    // this.referredSchema = referredSchema;
    // }

    /**
     * Builder class for {@link ReferenceSchema}.
     */
    public static class Builder extends Schema.Builder<ReferenceSchema> {
        private Schema.Builder<?> referredSchema;
        /**
         * The value of {@code "$ref"}
         */
        private String refValue = "";
        private ReferenceSchemaLoader referenceSchemaLoader;

        public Builder(String id) {
            super(id);
        }

        public Builder(SchemaLocation location) {
            super(location);
        }

        @Override
        public ReferenceSchema build() {
            return new ReferenceSchema(this);
        }

        public Builder referencedURL(String refValue) {
            this.refValue = refValue;
            return this;
        }

        public Builder referenceSchemaLoader(ReferenceSchemaLoader referenceSchemaLoader) {
            this.referenceSchemaLoader = referenceSchemaLoader;
            return this;
        }

        public Builder referredSchema(Schema.Builder<?> referredSchema) {
            this.referredSchema = referredSchema;
            return this;
        }
    }
}
