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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Array schema validator.
 */
@Getter
@Builder(builderClassName = "ArrayKeywordsBuilder")
@EqualsAndHashCode
public class ArrayKeywords implements SchemaKeywords {

    @Min(0)
    private final Integer minItems;

    @Min(0)
    private final Integer maxItems;

    private final boolean needsUniqueItems;

    @Valid
    private final List<JsonSchema> itemSchemas;

    @Valid
    private final JsonSchema allItemSchema;

    @Valid
    private final JsonSchema containsSchema;

    private final boolean requiresArray;

    @Valid
    private final JsonSchema schemaOfAdditionalItems;

    public Optional<JsonSchema> findAllItemSchema() {
        return Optional.ofNullable(allItemSchema);
    }

    public Optional<JsonSchema> findContainsSchema() {
        return Optional.ofNullable(containsSchema);
    }

    public Optional<JsonSchema> findSchemaOfAdditionalItems() {
        return Optional.ofNullable(schemaOfAdditionalItems);
    }

    public JsonSchema getAllItemSchema() {
        if (allItemSchema == null) {
            throw new NullPointerException("allItemSchema is null");
        }
        return allItemSchema;
    }

    @Override
    public Set<JsonSchemaType> getApplicableTypes() {
        return Collections.singleton(JsonSchemaType.ARRAY);
    }

    public JsonSchemaGenerator toJson(final JsonSchemaGenerator writer) {
        return writer.writeType(JsonSchemaType.ARRAY, requiresArray)
                .writeIfTrue(JsonSchemaKeyword.UNIQUE_ITEMS, needsUniqueItems)
                .optionalWrite(JsonSchemaKeyword.MIN_ITEMS, minItems)
                .optionalWrite(JsonSchemaKeyword.MAX_ITEMS, maxItems)
                .optionalWrite(JsonSchemaKeyword.ITEMS, allItemSchema)
                .optionalWrite(JsonSchemaKeyword.ITEMS, itemSchemas)
                .optionalWrite(JsonSchemaKeyword.ADDITIONAL_ITEMS, schemaOfAdditionalItems);
    }

    public JsonSchema getContainsSchema() {
        if (containsSchema == null) {
            throw new NullPointerException("containsSchema is null");
        }
        return containsSchema;
    }

    public JsonSchema getSchemaOfAdditionalItems() {
        if (schemaOfAdditionalItems == null) {
            throw new NullPointerException("schemaOfAdditionalItems is null");
        }
        return schemaOfAdditionalItems;
    }

    static class ArrayKeywordsBuilder {

    }
}
