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

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.NullSchema;
import io.dugnutt.jsonschema.six.NumberSchema;
import io.dugnutt.jsonschema.six.ResourceLoader;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.net.URI;
import java.util.Optional;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.schema.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.schema.JsonSchemaKeyword.TYPE;
import static io.dugnutt.jsonschema.six.ResourceLoader.DEFAULT;
import static io.dugnutt.jsonschema.six.ValidationErrorTest.loader;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.buildWithLocation;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectFailure;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.failureOf;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonArray;
import static io.dugnutt.jsonschema.utils.JsonUtils.readJsonObject;
import static org.assertj.core.api.Assertions.assertThat;
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
        final ArraySchema arraySchema = ArraySchema.builder().itemSchema(BooleanSchema.BOOLEAN_SCHEMA)
                .schemaOfAdditionalItems(NullSchema.NULL_SCHEMA)
                .build();
        expectSuccess(arraySchema, arrayTestCases.get("additionalItemsSchema"));
    }

    @Test
    public void additionalItemsSchemaFailure() {
        NullSchema nullSchema = buildWithLocation(NullSchema.builder());
        // if (itemSchemas == null) {
        //     itemSchemas = new ArrayList<>();
        // }
        // itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
        // return this;
        ArraySchema subject = buildWithLocation(
                ArraySchema.builder().itemSchema(buildWithLocation(BooleanSchema.builder()))
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
        arrayTestCases = ResourceLoader.DEFAULT.readObj("arraytestcases.json");
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
        ArraySchema.builder().itemSchema(BooleanSchema.BOOLEAN_SCHEMA).allItemSchema(NullSchema.NULL_SCHEMA)
                .build();
    }

    @Test
    public void tupleWithOneItem() {
        BooleanSchema boolSchema = buildWithLocation(BooleanSchema.builder());
        // if (itemSchemas == null) {
        //     itemSchemas = new ArrayList<>();
        // }
        // itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
        // return this;
        ArraySchema subject = buildWithLocation(ArraySchema.builder().itemSchema(boolSchema));
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

    @Test
    public void validate_WhenEqualNumbersWithDifferentLexicalRepresentations_ThenUnique() {
        final ArraySchema arraySchema = ArraySchema.builder()
                .uniqueItems(true)
                .requiresArray(true)
                .build();
        final ArrayKeywordValidator validator = new ArrayKeywordValidator(arraySchema);
        final Optional<ValidationError> errors = validator.validate(JsonUtils.readValue("[1.0, 1, 1.00]", JsonArray.class));
        Assert.assertFalse("Should have no errors", errors.isPresent());
    }

    @Test
    public void validate_WhenEqualNumbersWithSameLexicalRepresentations_ThenNotUnique() {
        final ArraySchema arraySchema = ArraySchema.builder().uniqueItems(true).requiresArray(true).build();
        final ArrayKeywordValidator validator = new ArrayKeywordValidator(arraySchema);
        final Optional<ValidationError> errors = validator.validate(JsonUtils.readValue("[1.0, 1.0, 1.00]", JsonArray.class));
        Assert.assertTrue("Should have no errors", errors.isPresent());
        Assert.assertEquals("Should have errors", "uniqueItems", errors.get().getKeyword().key());
    }

    @Test
    public void validate_WhenItemsSchemaHasEnum_AndArrayValueIsInEnumButWrongType_ThenFailWithTypeKeyword() {
        NumberSchema enumSchema = NumberSchema.builder()
                .requiresInteger(true)
                .enumValues(jsonArray(12, 24.3, 65))
                .build();

        final ArraySchema arraySchema = ArraySchema.builder()
                .allItemSchema(enumSchema)
                .build();

        JsonArray arrayValues = JsonUtils.jsonArray(24.3);
        Optional<ValidationError> error = SchemaValidatorFactory.createValidatorForSchema(arraySchema)
                .validate(arrayValues);
        assertThat(error.isPresent()).isTrue();

        SoftAssertions.assertSoftly(a -> {
            a.assertThat(error.get().getKeyword()).isEqualTo(TYPE);
            a.assertThat(error.get().getModel()).containsExactly(JsonSchemaType.INTEGER, JsonSchemaType.NUMBER);
        });
    }

    @Test
    public void validate_WhenItemsSchemaHasEnum_ThenDontEnforceLexicalMatching() {
        NumberSchema enumSchema = NumberSchema.builder()
                .enumValues(jsonArray(12, 24.3, 65))
                .build();

        final ArraySchema arraySchema = ArraySchema.builder()
                .allItemSchema(enumSchema)
                .build();

        JsonArray arrayValues = JsonUtils.jsonArray(24.30, 12);
        Optional<ValidationError> error = SchemaValidatorFactory.createValidatorForSchema(arraySchema)
                .validate(arrayValues);
        assertThat(error.isPresent())
                .describedAs("Error found: " + error.map(Object::toString).orElse(null))
                .isFalse();
    }

    @Test
    public void validate_WhenItemsSchemaHasEnum_ThenEnforceEachItem() {
        NumberSchema enumSchema = NumberSchema.builder()
                .enumValues(jsonArray(12, 24.3, 65))
                .build();

        final ArraySchema arraySchema = ArraySchema.builder()
                .allItemSchema(enumSchema)
                .build();

        JsonArray arrayValues = JsonUtils.jsonArray(24.30, 13);
        Optional<ValidationError> error = SchemaValidatorFactory.createValidatorForSchema(arraySchema)
                .validate(arrayValues);
        assertThat(error.isPresent())
                .describedAs("Should have failed but didn't")
                .isTrue();

        SoftAssertions.assertSoftly(assertj -> {
            ValidationError validationError = error.get();
            assertj.assertThat(validationError.getKeyword()).isEqualTo(ENUM);
            assertj.assertThat(validationError.getSchemaLocationURI()).isEqualTo(URI.create("#"));
            assertj.assertThat(validationError.getPointerToViolation()).isEqualTo("#/1");
        });
    }
}
