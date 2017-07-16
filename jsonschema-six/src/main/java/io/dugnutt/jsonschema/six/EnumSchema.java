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

import javax.json.JsonArray;
import java.util.Objects;

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
                    ObjectComparator.lexicalEquivalent(possibleValues, that.possibleValues) &&
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
    protected void propertiesToJson(JsonSchemaGenerator writer) {
        // writer.properties.put(TYPE.key(), provider.createValue(ENUM.key()));
        writer.writeKey(JsonSchemaProperty.ENUM);
        writer.array();
        possibleValues.forEach(writer::write);
        writer.endArray();
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
