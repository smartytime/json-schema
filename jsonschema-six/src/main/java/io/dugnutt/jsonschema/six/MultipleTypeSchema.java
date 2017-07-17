package io.dugnutt.jsonschema.six;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * When a schema's type isn't specified explicitly using tye "type" keyword, then we can't assign a specific schema
 * subclass to it.  Rather, we use this class type, and include all "possible" schemas, each of which will be consulted.
 * <p>
 * This is the downside of having concrete schema implementations, but it still leaves the code more usable and readable
 * than simply loading up keywords.
 */
@Getter
public class MultipleTypeSchema extends Schema {

    private final boolean requireOne;
    private final Map<JsonSchemaType, Schema> possibleSchemas;

    private MultipleTypeSchema(final MultipleTypeSchema.Builder builder) {
        super(builder);
        checkNotNull(builder.possibleSchemas, "possibleSchemas must not be null");
        this.possibleSchemas = builder.possibleSchemas;
        this.requireOne = builder.requireOne;
    }

    @Override
    protected void propertiesToJson(JsonSchemaGenerator writer) {
        //todo:ericm Do this
    }

    public static class Builder extends Schema.Builder<MultipleTypeSchema> {
        private final Map<JsonSchemaType, Schema> possibleSchemas = new HashMap<>();
        private boolean requireOne;


        public Builder requireOne(boolean requireOne) {
            this.requireOne = requireOne;
            return this;
        }

        public Builder addPossibleSchema(JsonSchemaType forType, Schema schema) {
            checkNotNull(forType, "forType must not be null");
            checkNotNull(schema, "schema must not be null");
            possibleSchemas.put(forType, schema);
            return this;
        }

        @Override
        public MultipleTypeSchema build() {
            return new MultipleTypeSchema(this);
        }
    }
}
