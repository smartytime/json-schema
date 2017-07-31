/*
 * Copyright (C) 2017 SBSP (http://sbsp.io)
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
package io.sbsp.jsonschema.six.keywords;

import io.sbsp.jsonschema.six.JsonSchemaGenerator;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Array schema validator.
 */
@Getter
@Builder(builderClassName = "ArrayKeywordsBuilder")
@EqualsAndHashCode(doNotUseGetters = true)
public class ArrayKeywords implements SchemaKeywords {

    @Min(0)
    private final Integer minItems;

    @Min(0)
    private final Integer maxItems;

    private final boolean needsUniqueItems;

    @Valid
    @NonNull
    private final List<Schema> itemSchemas;

    @Valid
    private final Schema allItemSchema;

    @Valid
    private final Schema containsSchema;

    @Valid
    private final Schema schemaOfAdditionalItems;

    public Optional<Schema> findAllItemSchema() {
        return Optional.ofNullable(allItemSchema);
    }

    public Optional<Schema> findContainsSchema() {
        return Optional.ofNullable(containsSchema);
    }

    public Optional<Schema> findSchemaOfAdditionalItems() {
        return Optional.ofNullable(schemaOfAdditionalItems);
    }

    public Schema getAllItemSchema() {
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
        return writer
                .writeIfTrue(JsonSchemaKeyword.UNIQUE_ITEMS, needsUniqueItems)
                .optionalWrite(JsonSchemaKeyword.MIN_ITEMS, minItems)
                .optionalWrite(JsonSchemaKeyword.MAX_ITEMS, maxItems)
                .optionalWrite(JsonSchemaKeyword.ITEMS, allItemSchema)
                .optionalWrite(JsonSchemaKeyword.ITEMS, itemSchemas)
                .optionalWrite(JsonSchemaKeyword.ADDITIONAL_ITEMS, schemaOfAdditionalItems);
    }

    public static class ArrayKeywordsBuilder {
        private List<Schema> itemSchemas = new ArrayList<>();
    }

    private static final ArrayKeywords BLANK_ARRAY_KEYWORDS = builder().build();

    public static ArrayKeywords getBlankArrayKeywords() {
        return BLANK_ARRAY_KEYWORDS;
    }
}