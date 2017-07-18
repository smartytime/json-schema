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

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import javax.json.JsonObject;
import javax.json.JsonValue;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonNumberValue;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.utils.JsonUtils.readJsonObject;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;
import static org.junit.Assert.assertEquals;

public class NumberSchemaTest {

    private final ResourceLoader loader = new ResourceLoader("/io/dugnutt/jsonschema/six/tostring/");

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(NumberSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void exclusiveMaximum() {
        NumberSchema subject = ValidationTestSupport.buildWithLocation(NumberSchema.builder().exclusiveMaximum(20));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("exclusiveMaximum")
                .input(20)
                .expect();
    }

    @Test
    public void exclusiveMinimum() {
        NumberSchema subject = ValidationTestSupport.buildWithLocation(NumberSchema.builder().exclusiveMinimum(10.0));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("exclusiveMinimum")
                .input(10)
                .expect();
    }

    @Test
    public void longNumber() {
        final NumberSchema schema = NumberSchema.builder().requiresNumber(true).build();
        createValidatorForSchema(schema).validate(jsonNumberValue(4278190207L));
    }

    @Test
    public void maximum() {
        NumberSchema subject = ValidationTestSupport.buildWithLocation(NumberSchema.builder().maximum(20.0));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("maximum")
                .input(21)
                .expect();
    }

    @Test
    public void minimumFailure() {
        NumberSchema subject = ValidationTestSupport.buildWithLocation(NumberSchema.builder().minimum(10.0));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("minimum")
                .input(9)
                .expect();
    }

    @Test
    public void multipleOfFailure() {
        NumberSchema subject = ValidationTestSupport.buildWithLocation(NumberSchema.builder().multipleOf(10));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("multipleOf")
                .input(15)
                .expect();
    }

    @Test
    public void notRequiresNumber() {
        final NumberSchema numberSchema = NumberSchema.builder()
                .requiresNumber(false)
                .build();
        expectSuccess(() -> createValidatorForSchema(numberSchema).validate(jsonStringValue("foo")));
        ;
    }

    @Test
    public void requiresIntegerSuccess() {
        final NumberSchema numberSchema = NumberSchema.builder().requiresNumber(true).build();
        expectSuccess(() -> createValidatorForSchema(numberSchema).validate(jsonNumberValue(10)));
        ;
    }

    @Test
    public void requiresIntegerFailure() {
        NumberSchema subject = ValidationTestSupport.buildWithLocation(NumberSchema.builder().requiresInteger(true));
        ValidationTestSupport.expectFailure(subject, 10.2f);
    }

    @Test
    public void smallMultipleOf() {
        final NumberSchema schema = NumberSchema.builder()
                .multipleOf(0.0001)
                .build();
        createValidatorForSchema(schema).validate(jsonNumberValue(0.0075));
    }

    @Test
    public void success() {
        final NumberSchema schema = NumberSchema.builder()
                .minimum(10.0)
                .exclusiveMaximum(11.0)
                .multipleOf(10)
                .build();
        createValidatorForSchema(schema).validate(jsonNumberValue(10.0));
    }

    @Test
    public void toStringNoExplicitType() {
        JsonObject rawSchemaJson = loader.readObjectWithBuilder("numberschema.json")
                .remove("type").build();
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void toStringReqInteger() {
        JsonObject rawSchemaJson = loader.readObjectWithBuilder("numberschema.json")
                .add("type", "number").build();
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void toStringTest() {
        JsonObject rawSchemaJson = loader.readObj("numberschema.json");
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void typeFailure() {
        ValidationTestSupport.failureOf(NumberSchema.builder())
                .expectedKeyword("type")
                .input(JsonValue.NULL)
                .expect();
    }
}
