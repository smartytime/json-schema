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

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonValue;
import java.util.List;
import java.util.Optional;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.*;
import static io.sbsp.jsonschema.validator.ValidationMocks.createTestValidator;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockNumberSchema;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class CombinedKeywordValidatorTest {

    private static final List<SchemaBuilder> SUBSCHEMAS = asList(
            mockNumberSchema().multipleOf(10),
            mockNumberSchema().multipleOf(3));

    @Test
    public void reportCauses() {
        final Schema parentSchema = jsonSchema().allOfSchemas(SUBSCHEMAS).build();
        final JsonValue subject = JsonUtils.readValue("24");
        Optional<ValidationError> error = createTestValidator(parentSchema).validate(subject);
        assertTrue("Has an error", error.isPresent());
        Assert.assertEquals(1, error.get().getCauses().size());
    }

    @Test
    public void validateAll() {
        ValidationTestSupport.failureOf(jsonSchema().allOfSchemas(SUBSCHEMAS).build())
                .input(20)
                .expectedKeyword("allOf")
                .expect();
    }

    @Test
    public void validateAny() {
        ValidationTestSupport.failureOf(jsonSchema().anyOfSchemas(SUBSCHEMAS).build())
                .input(5)
                .expectedKeyword("anyOf")
                .expect();
    }

    @Test
    public void validateOne() {
        ValidationTestSupport.failureOf(jsonSchema().oneOfSchemas(SUBSCHEMAS).build())
                .input(30)
                .expectedKeyword("oneOf")
                .expect();
    }
}
