/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dugnutt.jsonschema.six;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.StreamUtils.*;
import static java.util.Objects.requireNonNull;

/**
 * Validator for {@code allOf}, {@code oneOf}, {@code anyOf} schemas.
 */
@Getter
public class CombinedSchema extends Schema<CombinedSchema, CombinedSchema.Builder> {

    private final CombinedSchemaType combinedSchemaType;
    private final List<Schema> subSchemas;

    /**
     * Constructor.
     *
     * @param builder the builder containing the validation criterion and the subSchemas to be checked
     */
    public CombinedSchema(final Builder builder) {
        super(builder);
        checkNotNull(builder.subschemas, "builder.subschemas must not be null");
        this.combinedSchemaType = requireNonNull(builder.combinedSchemaType, "criterion cannot be null");
        this.subSchemas = Collections.unmodifiableList(builder.subschemas);
    }

    public static Builder allOf(final List<Schema> schemas) {
        return builder(schemas).combinedSchemaType(CombinedSchemaType.ALL_OF);
    }

    public static Builder anyOf(final List<Schema> schemas) {
        return builder(schemas).combinedSchemaType(CombinedSchemaType.ANY_OF);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(final Stream<Schema> subschemas) {
        return new Builder().subschemas(subschemas.collect(Collectors.toList()));
    }

    public static Builder builder(final List<Schema> subschemas) {
        return new Builder().subschemas(subschemas);
    }

    public static Builder oneOf(final List<Schema> schemas) {
        return builder(schemas).combinedSchemaType(CombinedSchemaType.ONE_OF);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subSchemas, combinedSchemaType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CombinedSchema) {
            CombinedSchema that = (CombinedSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(subSchemas, that.subSchemas) &&
                    Objects.equals(combinedSchemaType, that.combinedSchemaType) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected Builder internalToBuilder() {
        return new Builder().combinedSchemaType(this.combinedSchemaType)
                .subschemas(this.subSchemas);
    }

    /**
     * Each schema implementation must provide a completely cloned instance of itself with a new location.  This means that any
     * schema must also update the location for any of its child schemas.
     *
     * @param builder
     * @param schemaLocation
     * @return
     */
    @Override
    protected Builder internalWithLocation(SchemaLocation schemaLocation) {
        Builder builder = new Builder()
                .combinedSchemaType(this.combinedSchemaType);
        streamSchemaWithLocation(subSchemas, schemaLocation).forEach(builder::subschema);
        return builder;
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof CombinedSchema;
    }

    @Override
    protected void writePropertiesToJson(JsonSchemaGenerator writer) {
        writer.optionalWrite(combinedSchemaType.getKeyword(), subSchemas);
    }

    /**
     * Builder class for {@link CombinedSchema}.
     */
    public static class Builder extends Schema.Builder<CombinedSchema, CombinedSchema.Builder> {
        private final List<Schema> subschemas = new ArrayList<>();
        private CombinedSchemaType combinedSchemaType;

        @Override
        public CombinedSchema build() {
            return new CombinedSchema(this);
        }

        @Override
        public Builder self() {
            return this;
        }

        public Builder combinedSchemaType(final CombinedSchemaType combinedSchemaType) {
            this.combinedSchemaType = combinedSchemaType;
            return this;
        }

        public Builder subschema(final Schema subschema) {
            this.subschemas.add(subschema);
            return this;
        }

        public Builder subschemas(final List<Schema> subschemas) {
            this.subschemas.clear();
            this.subschemas.addAll(subschemas);
            return this;
        }
    }
}
