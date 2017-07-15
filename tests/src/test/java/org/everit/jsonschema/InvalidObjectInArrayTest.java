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

import org.everit.jsoniter.jsr353.JsoniterProvider;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.utils.JsonUtils;
import org.everit.jsonschema.validator.SchemaValidator;
import org.everit.jsonschema.validator.SchemaValidatorFactory;
import org.everit.jsonschema.validator.ValidationError;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Optional;

import static org.everit.jsonschema.loader.SchemaFactory.schemaFactory;

public class InvalidObjectInArrayTest {

    @Test
    public void test() {
        Schema schema = schemaFactory(new JsoniterProvider()).load(readObject("schema.json"));
        JsonObject subject = readObject("subject.json");
        SchemaValidator<?> validator = SchemaValidatorFactory.findValidator(schema);
        Optional<ValidationError> errors = validator.validate(subject);
        Assert.assertTrue("did not throw exception", errors.isPresent());
        Assert.assertEquals("#/notification/target/apps/0/id", errors.get().getPointerToViolation());
    }

    private JsonObject readObject(final String fileName) {
        return JsonUtils.readObject(getClass().getResourceAsStream("/org/everit/json/schema/invalidobjectinarray/" + fileName));
    }
}
