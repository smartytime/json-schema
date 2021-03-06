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
package io.sbsp.jsonschema;

import io.sbsp.jsonschema.loading.JsonSchemaFactory;
import io.sbsp.jsonschema.utils.JsonUtils;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.util.Optional;

import static org.junit.Assert.assertFalse;

public class EmptyObjectTest {
    @Test
    public void validateEmptyObject() {
        JsonObject jsonSubject = JsonUtils.readJsonObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {}\n" +
                "}");
        JsonObject schemaJson = ResourceLoader.DEFAULT.readObj("json-schema-draft-06.json");

        Schema schema = JsonSchemaFactory.schemaFactory(JsonProvider.provider()).load(schemaJson);
        SchemaValidator validator = ValidationMocks.createTestValidator(schema);
        Optional<ValidationError> errors = validator.validate(jsonSubject);
        assertFalse(errors.isPresent());
    }
}
