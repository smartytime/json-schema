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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@code Null} schema validator.
 */
public class NullSchema extends Schema {

    public static final NullSchema NULL_SCHEMA = builder().build();

    public NullSchema(final Builder builder) {
        super(builder);
    }

    public static Builder builder(String uri) {
        checkNotNull(uri, "uri must not be null");
        return new Builder();
    }

    public static Builder builder() { return new Builder(); }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NullSchema) {
            NullSchema that = (NullSchema) o;
            return that.canEqual(this) && super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NullSchema;
    }

    @Override
    protected void writePropertiesToJson(JsonSchemaGenerator writer) {
        writer.writeType(JsonSchemaType.NULL, true);
    }

    /**
     * Builder class for {@link NullSchema}.
     */
    public static class Builder extends Schema.Builder<NullSchema> {




        @Override
        public NullSchema build() {
            return new NullSchema(this);
        }
    }
}
