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

import java.net.URI;
import java.util.Objects;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static java.util.Objects.requireNonNull;

/**
 * {@code Not} schema validator.
 */
public class NotSchema extends Schema {

    private final Schema mustNotMatch;

    public NotSchema(final Builder builder) {
        super(builder);
        this.mustNotMatch = requireNonNull(builder.mustNotMatch, "mustNotMatch cannot be null");
    }

    public static Builder builder(SchemaLocation location) {
        return new Builder(location);
    }

    public Schema getMustNotMatch() {
        return mustNotMatch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mustNotMatch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NotSchema) {
            NotSchema that = (NotSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(mustNotMatch, that.mustNotMatch) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NotSchema;
    }

    @Override
    protected void writePropertiesToJson(JsonSchemaGenerator writer) {
        writer.write(NOT, mustNotMatch);
    }

    /**
     * Builder class for {@link NotSchema}.
     */
    public static class Builder extends Schema.Builder<NotSchema> {
        private Schema mustNotMatch;

        public Builder(String id) {
            super(id);
        }

        public Builder(SchemaLocation location) {
            super(location);
        }

        @Override
        public NotSchema build() {
            return new NotSchema(this);
        }

        public Builder mustNotMatch(final Schema mustNotMatch) {
            this.mustNotMatch = mustNotMatch;
            return this;
        }
    }
}
