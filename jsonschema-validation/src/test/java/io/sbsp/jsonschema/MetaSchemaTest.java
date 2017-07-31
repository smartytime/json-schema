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

import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.utils.JsonUtils;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationError;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Optional;

import static io.sbsp.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.sbsp.jsonschema.validator.ValidationMocks.createTestValidator;

public class MetaSchemaTest {

    @Test
    public void validateMetaSchema() {
        JsonObject jsonSchema = JsonUtils.readResourceAsJson("/io/sbsp/jsonschema/json-schema-draft-06.json", JsonObject.class);
        Schema schema = schemaFactory().load(jsonSchema);
        final SchemaValidator testValidator = createTestValidator(schema);
        final Optional<ValidationError> error = testValidator.validate(jsonSchema);
        if (error.isPresent()) {
            Assert.fail("Found errors: " + error.get().toJson().toString());
        }

    }
}
