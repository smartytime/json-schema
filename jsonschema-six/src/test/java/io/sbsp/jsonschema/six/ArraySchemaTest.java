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
package io.sbsp.jsonschema.six;

import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;

public class ArraySchemaTest {

    @Test(expected = SchemaException.class)
    public void tupleAndListFailure() {
        // if (itemSchemas == null) {
        //     itemSchemas = new ArrayList<>();
        // }
        // itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
        // return this;
        JsonSchemaBuilder.jsonSchema().allItemSchema(JsonSchemaBuilder.jsonSchema().constValueString("Foo"))
                .itemSchemas(newArrayList(JsonSchemaBuilder.jsonSchema().constValueDouble(23.2)))
                .build();
    }
}
