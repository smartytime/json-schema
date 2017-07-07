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
package org.everit.jsonschema.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Validator for {@code allOf}, {@code oneOf}, {@code anyOf} schemas.
 */
public class CombinedSchema extends Schema {

    private final CombinedSchemaType combinedSchemaType;
    private final Collection<Schema> subschemas;

    /**
     * Constructor.
     *
     * @param builder the builder containing the validation criterion and the subschemas to be checked
     */
    public CombinedSchema(final Builder builder) {
        super(builder);
        this.combinedSchemaType = requireNonNull(builder.combinedSchemaType, "criterion cannot be null");
        this.subschemas = requireNonNull(builder.subschemas, "subschemas cannot be null");
    }

    public static Builder allOf(final Collection<Schema> schemas) {
        return builder(schemas).combinedSchemaType(CombinedSchemaType.AllOf);
    }

    public static Builder anyOf(final Collection<Schema> schemas) {
        return builder(schemas).combinedSchemaType(CombinedSchemaType.AnyOf);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(final Collection<Schema> subschemas) {
        return new Builder().subschemas(subschemas);
    }

    public static Builder oneOf(final Collection<Schema> schemas) {
        return builder(schemas).combinedSchemaType(CombinedSchemaType.OneOf);
    }

    @Override
    public boolean definesProperty(final String field) {
        List<Schema> matching = subschemas.stream()
                .filter(schema -> schema.definesProperty(field))
                .collect(Collectors.toList());

        //todo:ericm Figure this out
        return false;
        // return !getCriterion().validate(subschemas.size(), matching.size()).isPresent();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subschemas, combinedSchemaType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CombinedSchema) {
            CombinedSchema that = (CombinedSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(subschemas, that.subschemas) &&
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
    void describePropertiesTo(JsonSchemaWriter writer) {
        writer.key(combinedSchemaType.toString());
        writer.array();
        subschemas.forEach(subschema -> subschema.describeTo(writer));
        writer.endArray();
    }

    public CombinedSchemaType getCombinedSchemaType() {
        return combinedSchemaType;
    }

    public Collection<Schema> getSubschemas() {
        return subschemas;
    }

    /**
     * Builder class for {@link CombinedSchema}.
     */
    public static class Builder extends Schema.Builder<CombinedSchema> {

        private CombinedSchemaType combinedSchemaType;

        private Collection<Schema> subschemas = new ArrayList<>();

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

        public Builder subschemas(final Collection<Schema> subschemas) {
            this.subschemas = subschemas;
            return this;
        }
    }
}
