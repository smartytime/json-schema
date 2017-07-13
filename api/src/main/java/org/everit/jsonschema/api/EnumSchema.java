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

import org.everit.json.JsonElement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Enum schema validator.
 */
public class EnumSchema extends Schema {

    private final Set<Object> possibleValues;

    public EnumSchema(final Builder builder) {
        super(builder);
        possibleValues = Collections.unmodifiableSet(builder.possibleValues.stream()
                .map(JsonElement::raw)
                .collect(toSet()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<Object> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), possibleValues);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EnumSchema) {
            EnumSchema that = (EnumSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(possibleValues, that.possibleValues) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof EnumSchema;
    }

    @Override
    void describePropertiesTo(final JsonWriter writer) {
        writer.key("type");
        writer.value("enum");
        writer.key("enum");
        writer.array();
        possibleValues.forEach(writer::value);
        writer.endArray();
    }

    /**
     * Builder class for {@link EnumSchema}.
     */
    public static class Builder extends Schema.Builder<EnumSchema> {

        private Set<JsonElement<?>> possibleValues = new HashSet<>();

        @Override
        public EnumSchema build() {
            return new EnumSchema(this);
        }

        public Builder possibleValue(final JsonElement<?> possibleValue) {
            possibleValues.add(possibleValue);
            return this;
        }

        public Builder possibleValues(final Set<JsonElement<?>> possibleValues) {
            this.possibleValues = possibleValues;
            return this;
        }
    }
}
