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
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static io.sbsp.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.sbsp.jsonschema.Schema.JsonSchemaBuilder;
import static io.sbsp.jsonschema.Schema.jsonSchemaBuilder;
import static io.sbsp.jsonschema.Schema.jsonSchemaBuilderWithId;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ENUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TYPE;
import static io.sbsp.jsonschema.enums.JsonSchemaType.NULL;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonArray;
import static io.sbsp.jsonschema.utils.JsonUtils.readJsonObject;
import static io.sbsp.jsonschema.validator.ResourceLoader.DEFAULT;
import static io.sbsp.jsonschema.validator.ValidationErrorTest.loader;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockArraySchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockBooleanSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockNullSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockNumberSchema;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.buildWithLocation;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.expectFailure;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.expectSuccess;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.failureOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.Assert.assertEquals;

public class ArraySchemaValidatorTest {

    JsonObject arrayTestCases;

    @Test
    public void additionalItemsSchema() {
        // if (itemSchemas == null) {
        //     itemSchemas = new ArrayList<>();
        // }
        // itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
        // return this;
        final Schema arraySchema = jsonSchemaBuilder().itemSchema(mockBooleanSchema())
                .schemaOfAdditionalItems(mockNullSchema())
                .build();
        expectSuccess(arraySchema, arrayTestCases.get("additionalItemsSchema"));
    }

    @Test
    public void additionalItemsSchemaFailure() {
        JsonSchemaBuilder nullSchema = jsonSchemaBuilderWithId("nulls").type(NULL);

        Schema subject =
                jsonSchemaBuilder()
                        .itemSchemas(Collections.singletonList(mockBooleanSchema("#booleans")))
                        .schemaOfAdditionalItems(nullSchema)
                        .build();

        failureOf(subject)
                .expectedViolatedSchema(nullSchema.build())
                .expectedSchemaLocation("#/additionalItems")
                .expectedPointer("#/2")
                .expectedKeyword(TYPE)
                .input(arrayTestCases.get("additionalItemsSchemaFailure"))
                .expect();
    }

    @Before
    public void before() {
        arrayTestCases = ResourceLoader.DEFAULT.readObj("arraytestcases.json");
    }

    @Test
    public void booleanItems() {
        Schema subject = jsonSchemaBuilder().allItemSchema(mockBooleanSchema()).build();
        expectFailure(subject, mockBooleanSchema().build(), "#/2", arrayTestCases.get("boolArrFailure"));
    }

    @Test
    public void doesNotRequireExplicitArray() {
        final Schema arraySchema = jsonSchemaBuilder()
                .needsUniqueItems(true)
                .build();
        expectSuccess(arraySchema, arrayTestCases.get("doesNotRequireExplicitArray"));
    }

    @Test
    public void maxItems() {
        Schema subject = buildWithLocation(jsonSchemaBuilder().maxItems(0));
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
        Schema subject = buildWithLocation(jsonSchemaBuilder().minItems(2));
        failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("minItems")
                .input(arrayTestCases.get("onlyOneItem"))
                .expect();
    }

    @Test
    public void noItemSchema() {
        final Schema schema = jsonSchemaBuilder().build();
        expectSuccess(schema, arrayTestCases.get("noItemSchema"));
    }

    @Test
    public void nonUniqueArrayOfArrays() {
        Schema subject = buildWithLocation(jsonSchemaBuilder().needsUniqueItems(true));
        failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("uniqueItems")
                .input(arrayTestCases.get("nonUniqueArrayOfArrays"))
                .expect();
    }

    @Test
    public void toStringAdditionalItems() {
        final JsonObject addtlProps = JsonUtils.jsonObjectBuilder().add("type", "boolean").build();
        final JsonObject rawSchemaJson = DEFAULT.readObjectWithBuilder("tostring/arrayschema-list.json")
                .remove("items")
                .add("additionalItems", addtlProps)
                .build();
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(addtlProps, readJsonObject(actual).getJsonObject("additionalItems"));
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
        // if (itemSchemas == null) {
        //     itemSchemas = new ArrayList<>();
        // }
        // itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
        // return this;
        jsonSchemaBuilder().itemSchema(mockBooleanSchema()).allItemSchema(mockNullSchema())
                .build();
    }

    @Test
    public void tupleWithOneItem() {
        // if (itemSchemas == null) {
        //     itemSchemas = new ArrayList<>();
        // }
        // itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
        // return this;
        Schema subject = jsonSchemaBuilder().itemSchema(mockBooleanSchema()).build();
        final Schema expectedSchema = subject.getArrayKeywords().getItemSchemas().get(0);

        failureOf(subject)
                .expectedViolatedSchema(expectedSchema)
                .expectedSchemaLocation("#/items/0")
                .expectedPointer("#/0")
                .input(arrayTestCases.get("tupleWithOneItem"))
                .expect();
    }

    @Test
    public void typeFailure() {
        failureOf(mockArraySchema().build())
                .expectedKeyword("type")
                .input(true)
                .expect();
    }

    @Test
    public void uniqueItemsObjectViolation() {
        Schema subject = jsonSchemaBuilder().needsUniqueItems(true).build();
        expectFailure(subject, "#", arrayTestCases.get("nonUniqueObjects"));
    }

    @Test
    public void uniqueItemsViolation() {
        Schema subject = jsonSchemaBuilder().needsUniqueItems(true).build();
        expectFailure(subject, "#", arrayTestCases.get("nonUniqueItems"));
    }

    @Test
    public void uniqueItemsWithSameToString() {
        final Schema schema = jsonSchemaBuilder().needsUniqueItems(true).build();
        expectSuccess(schema, arrayTestCases.get("uniqueItemsWithSameToString"));
    }

    @Test
    public void uniqueObjectValues() {
        final Schema schema = jsonSchemaBuilder().needsUniqueItems(true).build();
        expectSuccess(schema, arrayTestCases.get("uniqueObjectValues"));
    }

    @Test
    public void validate_WhenEqualNumbersWithDifferentLexicalRepresentations_ThenUnique() {
        final Schema arraySchema = mockArraySchema().needsUniqueItems(true).build();
        SchemaValidator validator = createTestValidator(arraySchema);
        JsonArray subject = JsonUtils.readValue("[1.0, 1, 1.00]", JsonArray.class);

        Optional<ValidationError> errors = validator.validate(subject);
        Assert.assertFalse("Should have no errors", errors.isPresent());
    }

    @Test
    public void validate_WhenEqualNumbersWithSameLexicalRepresentations_ThenNotUnique() {
        final Schema arraySchema = mockArraySchema().needsUniqueItems(true).build();
        SchemaValidator validator = createTestValidator(arraySchema);
        JsonArray subject = JsonUtils.readValue("[1.0, 1.0, 1.00]", JsonArray.class);
        final Optional<ValidationError> errors = validator.validate(subject);
        Assert.assertTrue("Should have no errors", errors.isPresent());
        Assert.assertEquals("Should have errors", "uniqueItems", errors.get().getKeyword().key());
    }

    @Test
    public void validate_WhenItemsSchemaHasEnum_AndArrayValueIsInEnumButWrongType_ThenFailWithTypeKeyword() {
        JsonSchemaBuilder enumSchema = ValidationMocks.mockIntegerSchema()
                .enumValues(jsonArray(12, 24.3, 65));

        final Schema arraySchema = jsonSchemaBuilder()
                .allItemSchema(enumSchema)
                .build();

        JsonArray arrayValues = JsonUtils.jsonArray(24.3);
        Optional<ValidationError> error = createTestValidator(arraySchema).validate(arrayValues);

        assertThat(error.isPresent()).isTrue();

        assertSoftly(a -> {
            a.assertThat(error.get().getKeyword()).isEqualTo(TYPE);
            a.assertThat(error.get().getModel()).containsExactly(JsonSchemaType.INTEGER, JsonSchemaType.NUMBER);
        });
    }

    @Test
    public void validate_WhenItemsSchemaHasEnum_ThenDontEnforceLexicalMatching() {
        JsonSchemaBuilder enumSchema = mockNumberSchema()
                .enumValues(jsonArray(12, 24.3, 65));

        final Schema arraySchema = jsonSchemaBuilder()
                .allItemSchema(enumSchema)
                .build();

        JsonArray arrayValues = JsonUtils.jsonArray(24.30, 12);
        Optional<ValidationError> error = createTestValidator(arraySchema).validate(arrayValues);
        assertThat(error.isPresent())
                .describedAs("Error found: " + error.map(Object::toString).orElse(null))
                .isFalse();
    }

    @Test
    public void validate_WhenItemsSchemaHasEnum_ThenEnforceEachItem() {
        JsonSchemaBuilder enumSchema = mockNumberSchema()
                .enumValues(jsonArray(12, 24.3, 65));


        final Schema arraySchema = jsonSchemaBuilder()
                .allItemSchema(enumSchema)
                .build();

        JsonArray arrayValues = JsonUtils.jsonArray(24.30, 13);
        Optional<ValidationError> error = createTestValidator(arraySchema)
                .validate(arrayValues);
        assertThat(error.isPresent())
                .describedAs("Should have failed but didn't")
                .isTrue();

        assertSoftly(assertj -> {
            ValidationError validationError = error.get();
            assertj.assertThat(validationError.getKeyword()).isEqualTo(ENUM);
            assertj.assertThat(validationError.getSchemaLocationURI()).isEqualTo(URI.create("#/items"));
            assertj.assertThat(validationError.getPointerToViolation()).isEqualTo("#/1");
        });
    }

    private SchemaValidator createTestValidator(Schema schema) {
        return SchemaValidatorFactory.builder().build().createValidator(schema);
    }
}
