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
package io.sbsp.jsonschema.validator;

import com.google.common.base.Strings;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchemaBuilderWithId;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$ID;
import static io.sbsp.jsonschema.validator.ValidationMocks.createTestValidator;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.expectSuccess;

public class EmptySchemaTest {

    @Test
    public void testAllGenericProps() {
        JsonObject actual = json("my title", "my description", "my/id");
        Assert.assertEquals(3, actual.keySet().size());
    }

    @Test
    public void testBuilder() {
        Assert.assertEquals(jsonSchema().build(), jsonSchema().build());
    }

    @Test
    public void testOnlyId() {
        JsonObject actual = json(null, null, "my/id");
        Assert.assertEquals(1, actual.keySet().size());
        Assert.assertEquals("my/id", actual.getString($ID.key()));
    }

    @Test
    public void testOnlySchemaDescription() {
        JsonObject actual = json(null, "descr", null);
        Assert.assertEquals(1, actual.keySet().size());
        Assert.assertEquals("descr", actual.getString("description"));
    }

    @Test
    public void testOnlyTitle() {
        JsonObject actual = json("my title", null, null);
        Assert.assertEquals(1, actual.keySet().size());
        Assert.assertEquals("my title", actual.getString("title"));
    }

    @Test
    public void testToString() {
        Assert.assertEquals("{}", jsonSchema().build().toString());
    }

    @Test
    public void testValidate() {
        expectSuccess(() -> createTestValidator(jsonSchema().build()).validate(JsonUtils.jsonStringValue("something")));
    }

    private JsonObject json(final String title, final String description, final String id) {
        final JsonSchemaBuilder builder;
        if (!Strings.isNullOrEmpty(id)) {
            builder = jsonSchemaBuilderWithId(id);
        } else {
            builder = jsonSchema();
        }
        String jsonFromString = builder
                .title(title)
                .description(description)
                .build()
                .toString();
        return JsonUtils.readJsonObject(jsonFromString);
    }
}
