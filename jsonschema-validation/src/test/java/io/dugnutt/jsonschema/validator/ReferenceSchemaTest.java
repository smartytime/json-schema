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
package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Test;

import javax.json.JsonObject;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static org.junit.Assert.assertEquals;

public class ReferenceSchemaTest {

    @Test
    public void toStringTest() {
        JsonObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/ref.json");
        String actual = schemaFactory().load(rawSchemaJson).toString();
        System.out.println(actual);
        assertEquals(rawSchemaJson.get("properties"),
                JsonUtils.readJsonObject(actual).getJsonObject("properties"));
    }
}
