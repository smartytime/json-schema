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
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.ResourceLoader.DEFAULT;
import static io.dugnutt.jsonschema.six.ValidationErrorTest.loader;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.buildWithLocation;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectFailure;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.failureOf;
import static io.dugnutt.jsonschema.utils.JsonUtils.readJsonObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ArraySchemaTest {

    JsonObject arrayTestCases;

    @Test
    public void additionalItemsSchema() {
        final ArraySchema arraySchema = ArraySchema.builder()
                .addItemSchema(BooleanSchema.BOOLEAN_SCHEMA)
                .schemaOfAdditionalItems(NullSchema.INSTANCE)
                .build();
        expectSuccess(arraySchema, arrayTestCases.get("additionalItemsSchema"));
    }

    @Test
    public void additionalItemsSchemaFailure() {
        NullSchema nullSchema = buildWithLocation(NullSchema.builder());
        ArraySchema subject = buildWithLocation(
                ArraySchema.builder()
                        .addItemSchema(buildWithLocation(BooleanSchema.builder()))
                        .schemaOfAdditionalItems(nullSchema)
        );
        failureOf(subject)
                .expectedViolatedSchema(nullSchema)
                .expectedPointer("#/2")
//                 .expectedKeyword("additionalItems")
                .input(arrayTestCases.get("additionalItemsSchemaFailure"))
                .expect();
    }

    @Before
    public void before() {
        arrayTestCases = JsonUtils.readValue(getClass().getResourceAsStream("arraytestcases.json"),
                JsonObject.class);
    }

    @Test
    public void booleanItems() {
        ArraySchema subject = ArraySchema.builder().allItemSchema(BooleanSchema.BOOLEAN_SCHEMA).build();
        expectFailure(subject, BooleanSchema.BOOLEAN_SCHEMA, "#/2", arrayTestCases.get("boolArrFailure"));
    }

    @Test
    public void doesNotRequireExplicitArray() {
        final ArraySchema arraySchema = ArraySchema.builder()
                .requiresArray(false)
                .uniqueItems(true)
                .build();
        expectSuccess(arraySchema, arrayTestCases.get("doesNotRequireExplicitArray"));
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(ArraySchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void maxItems() {
        ArraySchema subject = buildWithLocation(ArraySchema.builder().maxItems(0));
        failureOf(subject)
                .schema(subject)
                .expectedPointer("#")
                .expectedKeyword("maxItems")
                .expectedMessageFragment("expected maximum item count: 0, found: 1")
                .input(arrayTestCases.get("onlyOneItem"))
                .expect();
    }

    @Test
    public void minItems() {
        ArraySchema subject = buildWithLocation(ArraySchema.builder().minItems(2));
        failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("minItems")
                .input(arrayTestCases.get("onlyOneItem"))
                .expect();
    }

    @Test
    public void noAdditionalItems() {
        ArraySchema subject = ArraySchema.builder()
                .additionalItems(false)
                .addItemSchema(BooleanSchema.BOOLEAN_SCHEMA)
                .addItemSchema(NullSchema.INSTANCE)
                .build();
        expectFailure(subject, "#", arrayTestCases.get("twoItemTupleWithAdditional"));
    }

    @Test
    public void noItemSchema() {
        final ArraySchema schema = ArraySchema.builder().build();
        expectSuccess(schema, arrayTestCases.get("noItemSchema"));
    }

    @Test
    public void nonUniqueArrayOfArrays() {
        ArraySchema subject = buildWithLocation(ArraySchema.builder().uniqueItems(true));
        failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("uniqueItems")
                .input(arrayTestCases.get("nonUniqueArrayOfArrays"))
                .expect();
    }

    @Test
    public void toStringAdditionalItems() {
        final JsonObject rawSchemaJson = DEFAULT.readObjectWithBuilder("tostring/arrayschema-list.json")
                .remove("items")
                .add("additionalItems", false)
                .build();
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertFalse(readJsonObject(actual).getBoolean("additionalItems"));
    }

    @Test
    public void toStringNoExplicitType() {
        JsonObject rawSchemaJson = loader.readObjectWithBuilder("tostring/arrayschema-list.json")
                .remove("type").build();
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void toStringTest() {
        JsonObject rawSchemaJson = loader.readObj("tostring/arrayschema-list.json");
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void toStringTupleSchema() {
        JsonObject rawSchemaJson = loader.readObj("tostring/arrayschema-tuple.json");
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test(expected = SchemaException.class)
    public void tupleAndListFailure() {
        ArraySchema.builder().addItemSchema(BooleanSchema.BOOLEAN_SCHEMA).allItemSchema(NullSchema.INSTANCE)
                .build();
    }

    @Test
    public void tupleWithOneItem() {
        BooleanSchema boolSchema = buildWithLocation(BooleanSchema.builder());
        ArraySchema subject = buildWithLocation(ArraySchema.builder().addItemSchema(boolSchema));
        failureOf(subject)
                .expectedViolatedSchema(boolSchema)
                .expectedPointer("#/0")
                .input(arrayTestCases.get("tupleWithOneItem"))
                .expect();
    }

    @Test
    public void typeFailure() {
        failureOf(ArraySchema.builder())
                .expectedKeyword("type")
                .input(true)
                .expect();
    }

    @Test
    public void uniqueItemsObjectViolation() {
        ArraySchema subject = ArraySchema.builder().uniqueItems(true).build();
        expectFailure(subject, "#", arrayTestCases.get("nonUniqueObjects"));
    }

    @Test
    public void uniqueItemsViolation() {
        ArraySchema subject = ArraySchema.builder().uniqueItems(true).build();
        expectFailure(subject, "#", arrayTestCases.get("nonUniqueItems"));
    }

    @Test
    public void uniqueItemsWithSameToString() {
        final ArraySchema schema = ArraySchema.builder().uniqueItems(true).build();
        expectSuccess(schema, arrayTestCases.get("uniqueItemsWithSameToString"));
    }

    @Test
    public void uniqueObjectValues() {
        final ArraySchema schema = ArraySchema.builder().uniqueItems(true).build();
        expectSuccess(schema, arrayTestCases.get("uniqueObjectValues"));
    }
}
