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

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import io.dugnutt.jsonschema.validator.ValidationError;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Optional;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.validator.ValidationMocks.createTestValidator;

public class MetaSchemaTest {

    @Test
    public void validateMetaSchema() {
        JsonObject jsonSchema = JsonUtils.readResourceAsJson("/io/dugnutt/jsonschema/json-schema-draft-06.json", JsonObject.class);
        Schema schema = schemaFactory().load(jsonSchema);
        final Optional<ValidationError> error = createTestValidator(schema).validate(jsonSchema);
        if (error.isPresent()) {
            Assert.fail("Found errors: " + error.toString());
        }

    }
}
