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

import static io.dugnutt.jsonschema.six.SchemaLocation.schemaLocation;

/**
 * Boolean schema validator.
 */
public class BooleanSchema extends Schema {

    public static BooleanSchema BOOLEAN_SCHEMA = builder(schemaLocation()).build();

    BooleanSchema(final Builder builder) {
        super(builder);
    }

    public static Builder builder(SchemaLocation location) {        return new Builder(location);    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof BooleanSchema;
    }

    @Override
    protected void writePropertiesToJson(JsonSchemaGenerator writer) {
        writer.writeType(JsonSchemaType.BOOLEAN, true);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BooleanSchema) {
            BooleanSchema that = (BooleanSchema) o;
            return that.canEqual(this) && super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Builder class for {@link BooleanSchema}.
     */
    public static class Builder extends Schema.Builder<BooleanSchema> {
        public Builder(String id) {
            super(id);
        }

        public Builder(SchemaLocation location) {
            super(location);
        }

        @Override
        public BooleanSchema build() {
            return new BooleanSchema(this);
        }
    }
}
