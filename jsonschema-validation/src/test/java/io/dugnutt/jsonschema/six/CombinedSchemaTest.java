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
package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.utils.JsonUtils;
import io.dugnutt.jsonschema.validator.CombinedSchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationError;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.six.ValidationTestSupport.verifyFailure;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.verifySuccess;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.findValidator;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class CombinedSchemaTest {

    private static final List<Schema> SUBSCHEMAS = asList(
            NumberSchema.builder().multipleOf(10).build(),
            NumberSchema.builder().multipleOf(3).build());

    public void allCriterionFailure() {
        verifyFailure(() -> CombinedSchemaValidator.ALL_CRITERION.validate(10, 1));
    }

    @Test
    public void allCriterionSuccess() {
        verifySuccess(() -> CombinedSchemaValidator.ALL_CRITERION.validate(10, 10));
    }

    public void anyCriterionFailure() {
        verifyFailure(() -> CombinedSchemaValidator.ANY_CRITERION.validate(10, 0));
    }

    @Test
    public void anyCriterionSuccess() {
        verifySuccess(() -> CombinedSchemaValidator.ANY_CRITERION.validate(10, 1));
    }

    public void anyOfInvalid() {
        verifyFailure(() -> {
            CombinedSchema combinedSchema = CombinedSchema.anyOf(asList(
                    StringSchema.builder().maxLength(2).build(),
                    StringSchema.builder().minLength(4).build()))
                    .build();
            return findValidator(combinedSchema)
                    .validate(JsonUtils.readValue("\"foo\""));
        });
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(CombinedSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void factories() {
        CombinedSchema.allOf(asList(BooleanSchema.INSTANCE));
        CombinedSchema.anyOf(asList(BooleanSchema.INSTANCE));
        CombinedSchema.oneOf(asList(BooleanSchema.INSTANCE));
    }

    public void oneCriterionFailure() {
        verifyFailure(() -> CombinedSchemaValidator.ONE_CRITERION.validate(10, 2));
    }

    @Test
    public void oneCriterionSuccess() {
        verifySuccess(() -> CombinedSchemaValidator.ONE_CRITERION.validate(10, 1));
    }

    @Test
    public void reportCauses() {
        CombinedSchema combinedSchema = CombinedSchema.allOf(SUBSCHEMAS).build();
        Optional<ValidationError> error = findValidator((CombinedSchema) combinedSchema).validate(JsonUtils.readValue("24"));
        assertTrue("Has an error", error.isPresent());
        Assert.assertEquals(1, error.get().getCauses().size());
    }

    @Test
    public void validateAll() {
        ValidationTestSupport.failureOf(CombinedSchema.allOf(SUBSCHEMAS))
                .input("20")
                .expectedKeyword("allOf")
                .expect();
    }

    @Test
    public void validateAny() {
        ValidationTestSupport.failureOf(CombinedSchema.anyOf(SUBSCHEMAS))
                .input("5")
                .expectedKeyword("anyOf")
                .expect();
    }

    @Test
    public void validateOne() {
        ValidationTestSupport.failureOf(CombinedSchema.oneOf(SUBSCHEMAS))
                .input("30")
                .expectedKeyword("oneOf")
                .expect();
    }
}
