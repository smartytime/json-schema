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
import org.junit.Test;

import javax.json.JsonObject;
import javax.json.JsonValue;

import static io.sbsp.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonNumberValue;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.sbsp.jsonschema.utils.JsonUtils.readJsonObject;
import static io.sbsp.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockIntegerSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockNumberSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockSchema;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.expectSuccess;
import static org.junit.Assert.assertEquals;

public class NumberSchemaTest {

    private final ResourceLoader loader = new ResourceLoader("/io/sbsp/jsonschema/six/tostring/");

    @Test
    public void exclusiveMaximum() {
        Schema subject = mockNumberSchema().exclusiveMaximum(20).build();
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("exclusiveMaximum")
                .input(20)
                .expect();
    }

    @Test
    public void exclusiveMinimum() {
        Schema subject = ValidationTestSupport.buildWithLocation(mockNumberSchema().exclusiveMinimum(10.0));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("exclusiveMinimum")
                .input(10)
                .expect();
    }

    @Test
    public void longNumber() {
        final Schema schema = mockNumberSchema().build();
        createValidatorForSchema(schema).validate(jsonNumberValue(4278190207L));
    }

    @Test
    public void maximum() {
        Schema subject = ValidationTestSupport.buildWithLocation(mockNumberSchema().maximum(20.0));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("maximum")
                .input(21)
                .expect();
    }

    @Test
    public void minimumFailure() {
        Schema subject = ValidationTestSupport.buildWithLocation(mockNumberSchema().minimum(10.0));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("minimum")
                .input(9)
                .expect();
    }

    @Test
    public void multipleOfFailure() {
        Schema subject = ValidationTestSupport.buildWithLocation(mockNumberSchema().multipleOf(10));
        ValidationTestSupport.failureOf(subject)
                .expectedKeyword("multipleOf")
                .input(15)
                .expect();
    }

    @Test
    public void notRequiresNumber() {
        final Schema numberSchema = mockSchema()
                .build();
        expectSuccess(() -> createValidatorForSchema(numberSchema).validate(jsonStringValue("foo")));
    }

    @Test
    public void requiresIntegerSuccess() {
        final Schema numberSchema = mockNumberSchema().build();
        expectSuccess(() -> createValidatorForSchema(numberSchema).validate(jsonNumberValue(10)));
    }

    @Test
    public void requiresIntegerFailure() {
        Schema subject = mockIntegerSchema().build();
        ValidationTestSupport.expectFailure(subject, 10.2f);
    }

    @Test
    public void smallMultipleOf() {
        final Schema schema = mockNumberSchema()
                .multipleOf(0.0001)
                .build();
        createValidatorForSchema(schema).validate(jsonNumberValue(0.0075));
    }

    @Test
    public void success() {
        final Schema schema = mockNumberSchema()
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
        ValidationTestSupport.failureOf(mockNumberSchema())
                .expectedKeyword("type")
                .input(JsonValue.NULL)
                .expect();
    }
}
