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
package org.martysoft.jsonschema.v6;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.ITEMS;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.MAX_ITEMS;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.MIN_ITEMS;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.NEEDS_ADDITIONAL_ITEMS;
import static org.martysoft.jsonschema.v6.JsonSchemaProperty.NEEDS_UNIQUE_ITEMS;

/**
 * Array schema validator.
 */
@Getter
public class ArraySchema extends Schema {

    private final Integer minItems;
    private final Integer maxItems;
    private final boolean needsUniqueItems;
    private final Schema allItemSchema;
    private final boolean needsAdditionalItems;
    private final List<Schema> itemSchemas;
    private final boolean requiresArray;
    private final Schema schemaOfAdditionalItems;

    /**
     * Constructor.
     *
     * @param builder contains validation criteria.
     */
    public ArraySchema(final Builder builder) {
        super(builder);
        this.minItems = builder.minItems;
        this.maxItems = builder.maxItems;
        this.needsUniqueItems = builder.uniqueItems;
        this.allItemSchema = builder.allItemSchema;
        this.itemSchemas = builder.itemSchemas;
        if (!builder.additionalItems && allItemSchema != null) {
            needsAdditionalItems = true;
        } else {
            needsAdditionalItems = builder.schemaOfAdditionalItems != null || builder.additionalItems;
        }
        this.schemaOfAdditionalItems = builder.schemaOfAdditionalItems;
        if (!(allItemSchema == null || itemSchemas == null)) {
            throw new SchemaException("cannot perform both tuple and list validation");
        }
        this.requiresArray = builder.requiresArray;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minItems, maxItems, needsUniqueItems, allItemSchema,
                needsAdditionalItems, itemSchemas, requiresArray, schemaOfAdditionalItems);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ArraySchema) {
            ArraySchema that = (ArraySchema) o;
            return that.canEqual(this) &&
                    needsUniqueItems == that.needsUniqueItems &&
                    needsAdditionalItems == that.needsAdditionalItems &&
                    requiresArray == that.requiresArray &&
                    Objects.equals(minItems, that.minItems) &&
                    Objects.equals(maxItems, that.maxItems) &&
                    Objects.equals(allItemSchema, that.allItemSchema) &&
                    Objects.equals(itemSchemas, that.itemSchemas) &&
                    Objects.equals(schemaOfAdditionalItems, that.schemaOfAdditionalItems) &&
                    super.equals(o);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof ArraySchema;
    }

    @Override
    protected void propertiesToJson(final JsonSchemaGenerator writer) {
        writer.writeType(JsonSchemaType.ARRAY, requiresArray)
                .writeIfTrue(NEEDS_UNIQUE_ITEMS, needsUniqueItems)
                .optionalWrite(MIN_ITEMS, minItems)
                .optionalWrite(MAX_ITEMS, maxItems)
                .writeIfFalse(NEEDS_ADDITIONAL_ITEMS, needsAdditionalItems)
                .optionalWrite(ITEMS, allItemSchema)
                .optionalWrite(ITEMS, itemSchemas)
                .optionalWrite(NEEDS_ADDITIONAL_ITEMS, schemaOfAdditionalItems);
    }

    /**
     * Builder class for {@link ArraySchema}.
     */
    public static class Builder extends Schema.Builder<ArraySchema> {

        private boolean requiresArray = true;

        private Integer minItems;

        private Integer maxItems;

        private boolean uniqueItems = false;

        private Schema allItemSchema;

        private List<Schema> itemSchemas = null;

        private boolean additionalItems = true;

        private Schema schemaOfAdditionalItems;

        /**
         * Adds an item schema for tuple validation. The array items of the subject under validation
         * will be matched to expected schemas by their index. In other words the {n}th
         * {@code addItemSchema()} invocation defines the expected schema of the {n}th item of the array
         * being validated.
         *
         * @param itemSchema the schema of the next item.
         * @return this
         */
        public Builder addItemSchema(final Schema itemSchema) {
            if (itemSchemas == null) {
                itemSchemas = new ArrayList<Schema>();
            }
            itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
            return this;
        }

        public Builder additionalItems(final boolean additionalItems) {
            this.additionalItems = additionalItems;
            return this;
        }

        public Builder allItemSchema(final Schema allItemSchema) {
            this.allItemSchema = allItemSchema;
            return this;
        }

        @Override
        public ArraySchema build() {
            return new ArraySchema(this);
        }

        public Builder maxItems(final Integer maxItems) {
            this.maxItems = maxItems;
            return this;
        }

        public Builder minItems(final Integer minItems) {
            this.minItems = minItems;
            return this;
        }

        public Builder requiresArray(final boolean requiresArray) {
            this.requiresArray = requiresArray;
            return this;
        }

        public Builder schemaOfAdditionalItems(final Schema schemaOfAdditionalItems) {
            this.schemaOfAdditionalItems = schemaOfAdditionalItems;
            return this;
        }

        public Builder uniqueItems(final boolean uniqueItems) {
            this.uniqueItems = uniqueItems;
            return this;
        }
    }
}
