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

import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.loader.SchemaFactory;
import org.everit.jsonschema.utils.JsonUtils;
import org.everit.jsonschema.validator.SchemaValidator;
import org.everit.jsonschema.validator.SchemaValidatorFactory;
import org.everit.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Optional;

import static org.junit.Assert.assertFalse;

public class EmptyObjectTest {
    @Test
    public void validateEmptyObject() {
        JsonObject jsonSubject = JsonUtils.readObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {}\n" +
                "}");
        JsonObject jsonElement = JsonUtils.readObject(MetaSchemaTest.class
                .getResourceAsStream("/org/everit/json/schema/json-schema-draft-04.json"));

        Schema schema = SchemaFactory.schemaFactory().load(jsonElement);
        SchemaValidator<?> validator = SchemaValidatorFactory.findValidator(schema);
        Optional<ValidationError> errors = validator.validate(jsonSubject);
        assertFalse(errors.isPresent());
    }
}
