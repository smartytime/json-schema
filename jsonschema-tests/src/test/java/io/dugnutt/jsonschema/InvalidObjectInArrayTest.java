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
package io.dugnutt.jsonschema;

import io.dugnutt.jsonschema.validator.ValidationError;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Optional;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;

public class InvalidObjectInArrayTest {

    @Test
    public void test() {
        Schema schema = schemaFactory().load(readObject("schema.json"));
        JsonObject subject = readObject("subject.json");
        SchemaValidator<?> validator = SchemaValidatorFactory.createValidatorForSchema(schema);
        Optional<ValidationError> errors = validator.validate(subject);
        Assert.assertTrue("did not throw exception", errors.isPresent());
        Assert.assertEquals("#/notification/target/apps/0", errors.get().getPointerToViolation());
    }

    private JsonObject readObject(final String fileName) {
        return JsonUtils.readJsonObject(getClass().getResourceAsStream("/io/dugnutt/json/schema/invalidobjectinarray/" + fileName));
    }
}
