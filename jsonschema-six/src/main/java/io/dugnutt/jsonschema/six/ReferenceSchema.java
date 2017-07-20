package io.dugnutt.jsonschema.six;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
        final SchemaLocation currentLocation = builder.getLocation();
        this.absoluteReferenceURI = currentLocation.getResolutionScope().resolve(referenceURI);
        final SchemaFactory loader = builder.referenceSchemaLoader;
        if (loader != null) {
            this.referredSchema = loader.dereferenceSchema(currentLocation.getDocumentURI(), this, builder.rootJsonObject);
        } else {
            referredSchema = null;
        }
    }

    public Optional<Schema> getFullyDereferencedSchema() {
        Set<ReferenceSchema> encountered = new HashSet<>();
        ReferenceSchema schema = this;
        while (encountered.add(schema)) {
            Schema dereferencedSchema = schema.getReferredSchema().orElse(null);
            if (dereferencedSchema == null) {
                return Optional.empty();
            } else if (dereferencedSchema instanceof ReferenceSchema) {
                schema = (ReferenceSchema) dereferencedSchema;
            } else {
                return Optional.of(dereferencedSchema);
            }
        }
        throw new SchemaException(absoluteReferenceURI, "Infinite recursion found between schemas.  Probably bug: %s", encountered);
    }

    public static Builder builder(SchemaLocation location) {
        return new Builder(location);
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

    public URI getAbsoluteReferenceURI() {
        return absoluteReferenceURI;
    }

    public URI getReferenceURI() {
        return referenceURI;
    }

    public Optional<Schema> getReferredSchema() {
        return Optional.ofNullable(referredSchema);
    }

    /**
     * Builder class for {@link ReferenceSchema}.
     */
    public static class Builder extends Schema.Builder<ReferenceSchema> {
        private Schema.Builder<?> referredSchema;
        /**
         * The value of {@code "$ref"}
         */
        private String refValue = "";
        private SchemaFactory referenceSchemaLoader;
        private JsonObject rootJsonObject;

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

        public Builder referenceSchemaLoader(SchemaFactory referenceSchemaLoader, JsonObject rootJsonObject) {
            checkNotNull(referenceSchemaLoader, "referenceSchemaLoader must not be null");
            checkNotNull(rootJsonObject, "rootJsonObject must not be null");
            this.referenceSchemaLoader = referenceSchemaLoader;
            this.rootJsonObject = rootJsonObject;
            return this;
        }

        public Builder referencedURL(String refValue) {
            this.refValue = refValue;
            return this;
        }

        public Builder referredSchema(Schema.Builder<?> referredSchema) {
            this.referredSchema = referredSchema;
            return this;
        }
    }
}
