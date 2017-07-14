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
package org.everit.jsonschema;

import org.everit.json.JsonElement;
import org.everit.json.JsonObject;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.loader.SchemaLoader;
import org.everit.jsonschema.loader.internal.DefaultSchemaClient;
import org.everit.jsonschema.loaders.jsoniter.JsoniterApi;
import org.everit.jsonschema.validator.SchemaValidator;
import org.everit.jsonschema.validator.SchemaValidatorFactory;
import org.everit.jsonschema.validator.ValidationError;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Optional;

import static org.junit.Assert.assertFalse;

public class EmptyObjectTest {
    @Test
    public void validateEmptyObject() {
        JsoniterApi jsoniterApi = new JsoniterApi();
        JsonObject<?> jsonSubject = jsoniterApi.readJson("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {}\n" +
                "}").asObject();
        JsonElement<?> jsonElement = jsoniterApi.readJson(MetaSchemaTest.class
                .getResourceAsStream("/org/everit/json/schema/json-schema-draft-04.json"), Charset.forName("UTF-8"));

        Schema schema = SchemaLoader.load(jsonElement.asObject(), new DefaultSchemaClient(), jsoniterApi);
        SchemaValidator<?> validator = SchemaValidatorFactory.findValidator(schema);
        Optional<ValidationError> errors = validator.validate(jsonSubject);
        assertFalse(errors.isPresent());
    }
}
