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
 * Boolean schema validator.
 */
public class BooleanSchema extends Schema<BooleanSchema, BooleanSchema.Builder> {

    public static BooleanSchema BOOLEAN_SCHEMA = builder().build();

    BooleanSchema(final Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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
        return new Builder();
    }

    @Override
    protected Builder internalToBuilder() {
        return new Builder();
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof BooleanSchema;
    }

    @Override
    protected void writePropertiesToJson(JsonSchemaGenerator writer) {
        writer.writeType(JsonSchemaType.BOOLEAN, true);
    }

    /**
     * Builder class for {@link BooleanSchema}.
     */
    public static class Builder extends Schema.Builder<BooleanSchema, Builder> {

        @Override
        public BooleanSchema build() {
            return new BooleanSchema(this);
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}
