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

/**
 * A schema not specifying any restrictions, ie. accepting any values.
 */
public class EmptySchema extends Schema {

    public static EmptySchema EMPTY_SCHEMA = builder(SchemaLocation.schemaLocation()).build();

    public EmptySchema(final Builder builder) {
        super(builder);
    }

    public static Builder builder(SchemaLocation location) {
        return new Builder(location);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EmptySchema) {
            EmptySchema that = (EmptySchema) o;
            return that.canEqual(this) && super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof EmptySchema;
    }

    /**
     * Builder class for {@link EmptySchema}.
     */
    public static class Builder extends Schema.Builder<EmptySchema> {
        public Builder(String id) {
            super(id);
        }

        public Builder(SchemaLocation location) {
            super(location);
        }

        @Override
        public EmptySchema build() {
            return new EmptySchema(this);
        }
    }
}
