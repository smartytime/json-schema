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

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonValue;
import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.dugnutt.jsonschema.validator.CombinedSchemaValidator.combinedSchemaValidator;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.DEFAULT_VALIDATOR;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockNumberSchema;
import static io.dugnutt.jsonschema.validator.ValidationMocks.pathAware;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class CombinedSchemaValidatorTest {

    private static final List<JsonSchemaBuilder> SUBSCHEMAS = asList(
            mockNumberSchema().multipleOf(10),
            mockNumberSchema().multipleOf(3));

    @Test
    public void reportCauses() {
        final Schema parentSchema = jsonSchemaBuilder().allOfSchemas(SUBSCHEMAS).build();
        final JsonValue subject = JsonUtils.readValue("24");
        Optional<ValidationError> error =
                combinedSchemaValidator().validate(pathAware(subject), parentSchema, DEFAULT_VALIDATOR, parentSchema.getAllOfSchemas(), ALL_OF);
        assertTrue("Has an error", error.isPresent());
        Assert.assertEquals(1, error.get().getCauses().size());
    }

    @Test
    public void validateAll() {
        ValidationTestSupport.failureOf(jsonSchemaBuilder().allOfSchemas(SUBSCHEMAS).build())
                .input("20")
                .expectedKeyword("allOf")
                .expect();
    }

    @Test
    public void validateAny() {
        ValidationTestSupport.failureOf(jsonSchemaBuilder().anyOfSchemas(SUBSCHEMAS).build())
                .input("5")
                .expectedKeyword("anyOf")
                .expect();
    }

    @Test
    public void validateOne() {
        ValidationTestSupport.failureOf(jsonSchemaBuilder().oneOfSchemas(SUBSCHEMAS).build())
                .input("30")
                .expectedKeyword("oneOf")
                .expect();
    }
}
