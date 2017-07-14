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

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Objects;

import static org.everit.jsonschema.api.JsonSchemaProperty.ENUM;
import static org.everit.jsonschema.api.JsonSchemaProperty.TYPE;

/**
 * Enum schema validator.
 */
public class EnumSchema extends Schema {

    private final JsonArray possibleValues;

    public EnumSchema(final Builder builder) {
        super(builder);
        possibleValues = builder.possibleValues;
    }

    public static Builder builder() {
        return new Builder();
    }

    public JsonArray getPossibleValues() {
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

    void appendPropertiesTo(final JsonObject properties) {
        properties.put(TYPE.key(), provider.createValue(ENUM.key()));
        properties.put(ENUM.key(), possibleValues);
    }

    /**
     * Builder class for {@link EnumSchema}.
     */
    public static class Builder extends Schema.Builder<EnumSchema> {

        private JsonArray possibleValues;

        @Override
        public EnumSchema build() {
            return new EnumSchema(this);
        }

        public Builder possibleValues(final JsonArray possibleValues) {
            this.possibleValues = possibleValues;
            return this;
        }
    }
}
