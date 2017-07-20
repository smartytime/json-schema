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

import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.NumberSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StringSchema;
import io.dugnutt.jsonschema.six.ValidationTestSupport;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.SchemaLocation.schemaLocation;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class CombinedSchemaValidatorTest {

    private static final List<Schema> SUBSCHEMAS = asList(
            NumberSchema.builder(schemaLocation()).multipleOf(10).build(),
            NumberSchema.builder(schemaLocation()).multipleOf(3).build());

    private static final CombinedSchema ALL_CRITERION = CombinedSchema.allOf(schemaLocation(), SUBSCHEMAS).build();
    public void anyOfInvalid() {
        ValidationTestSupport.verifyFailure(() -> {
            CombinedSchema combinedSchema = CombinedSchema.anyOf(schemaLocation(), asList(
                    StringSchema.builder(schemaLocation()).maxLength(2).build(),
                    StringSchema.builder(schemaLocation()).minLength(4).build()))
                    .build();
            return SchemaValidatorFactory.createValidatorForSchema(combinedSchema)
                    .validate(JsonUtils.readValue("\"foo\""));
        });
    }

    @Test
    public void factories() {
        CombinedSchema.allOf(schemaLocation(), asList(BooleanSchema.BOOLEAN_SCHEMA));
        CombinedSchema.anyOf(schemaLocation(), asList(BooleanSchema.BOOLEAN_SCHEMA));
        CombinedSchema.oneOf(schemaLocation(), asList(BooleanSchema.BOOLEAN_SCHEMA));
    }

    @Test
    public void reportCauses() {
        CombinedSchema combinedSchema = CombinedSchema.allOf(schemaLocation(), SUBSCHEMAS).build();
        Optional<ValidationError> error = SchemaValidatorFactory.createValidatorForSchema((CombinedSchema) combinedSchema).validate(JsonUtils.readValue("24"));
        assertTrue("Has an error", error.isPresent());
        Assert.assertEquals(1, error.get().getCauses().size());
    }

    @Test
    public void validateAll() {
        ValidationTestSupport.failureOf(CombinedSchema.allOf(schemaLocation(), SUBSCHEMAS))
                .input("20")
                .expectedKeyword("allOf")
                .expect();
    }

    @Test
    public void validateAny() {
        ValidationTestSupport.failureOf(CombinedSchema.anyOf(schemaLocation(), SUBSCHEMAS))
                .input("5")
                .expectedKeyword("anyOf")
                .expect();
    }

    @Test
    public void validateOne() {
        ValidationTestSupport.failureOf(CombinedSchema.oneOf(schemaLocation(), SUBSCHEMAS))
                .input("30")
                .expectedKeyword("oneOf")
                .expect();
    }
}
