package io.dugnutt.jsonschema.six;

import java.util.Objects;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static java.util.Objects.requireNonNull;

/**
 * This class is used to resolve JSON pointers.
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */
public class ReferenceSchema extends Schema {

    private final String refValue;
    private final Schema referredSchema;

    public ReferenceSchema(final Builder builder) {
        super(builder);
        this.refValue = requireNonNull(builder.refValue, "refValue cannot be null");
        this.referredSchema= null;
        // checkNotNull(builder.referredSchema, "builder.referredSchema must not be null");
        // this.referredSchema = builder.referredSchema.build();
    }

    public static Builder builder() {
        return new Builder();
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
        return Objects.hash(super.hashCode(), referredSchema, refValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ReferenceSchema) {
            ReferenceSchema that = (ReferenceSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(refValue, that.refValue) &&
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
        writer.write($REF, refValue);
    }

    public Schema getReferredSchema() {
        return referredSchema;
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

        /**
         * This method caches its result, so multiple invocations will return referentially the same
         * {@link ReferenceSchema} instance.
         */
        @Override
        public ReferenceSchema build() {
            return new ReferenceSchema(this);
        }

        public Builder refValue(String refValue) {
            this.refValue = refValue;
            return this;
        }

        public Builder referredSchema(Schema.Builder<?> referredSchema) {
            this.referredSchema = referredSchema;
            return this;
        }

    }
}