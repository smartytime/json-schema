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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Validator for {@code allOf}, {@code oneOf}, {@code anyOf} schemas.
 */
@Getter
public class CombinedSchema extends Schema {

    private final CombinedSchemaType combinedSchemaType;
    private final List<Schema> subSchemas;

    /**
     * Constructor.
     *
     * @param builder the builder containing the validation criterion and the subSchemas to be checked
     */
    public CombinedSchema(final Builder builder) {
        super(builder);
        this.combinedSchemaType = requireNonNull(builder.combinedSchemaType, "criterion cannot be null");
        this.subSchemas = requireNonNull(builder.subschemas, "subSchemas cannot be null");
    }

    public static Builder allOf(SchemaLocation location, final List<Schema> schemas) {
        return builder(location, schemas).combinedSchemaType(CombinedSchemaType.AllOf);
    }

    public static Builder anyOf(SchemaLocation location, final List<Schema> schemas) {
        return builder(location, schemas).combinedSchemaType(CombinedSchemaType.AnyOf);
    }

    public static Builder builder(SchemaLocation location) {
        return new Builder(location);
    }

    public static Builder builder(SchemaLocation location, final Stream<Schema> subschemas) {
        return new Builder(location).subschemas(subschemas.collect(Collectors.toList()));
    }

    public static Builder builder(SchemaLocation location, final List<Schema> subschemas) {
        return new Builder(location).subschemas(subschemas);
    }

    public static Builder oneOf(SchemaLocation location, final List<Schema> schemas) {
        return builder(location, schemas).combinedSchemaType(CombinedSchemaType.OneOf);
    }

    @Override
    public boolean definesProperty(final String field) {
        List<Schema> matching = subSchemas.stream()
                .filter(schema -> schema.definesProperty(field))
                .collect(Collectors.toList());

        //todo:ericm Figure this out
        return false;
        // return !getCriterion().validate(subSchemas.size(), matching.size()).isPresent();
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
    protected boolean canEqual(Object other) {
        return other instanceof CombinedSchema;
    }

    @Override
    protected void writePropertiesToJson(JsonSchemaGenerator writer) {
        writer.optionalWrite(combinedSchemaType.getProperty(), subSchemas);
    }

    /**
     * Builder class for {@link CombinedSchema}.
     */
    public static class Builder extends Schema.Builder<CombinedSchema> {
        private CombinedSchemaType combinedSchemaType;
        private List<Schema> subschemas = new ArrayList<>();

        public Builder(String id) {
            super(id);
        }

        public Builder(SchemaLocation location) {
            super(location);
        }

        @Override
        public CombinedSchema build() {
            return new CombinedSchema(this);
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
            this.subschemas = subschemas;
            return this;
        }
    }
}
