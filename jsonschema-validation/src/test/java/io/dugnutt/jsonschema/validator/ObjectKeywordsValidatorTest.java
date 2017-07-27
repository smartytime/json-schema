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

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.ObjectKeywords;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.experimental.var;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.List;
import java.util.concurrent.Callable;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TYPE;
import static io.dugnutt.jsonschema.six.JsonSchemaType.BOOLEAN;
import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilderWithId;
import static io.dugnutt.jsonschema.utils.JsonUtils.blankJsonObject;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.utils.JsonUtils.readJsonObject;
import static io.dugnutt.jsonschema.validator.ObjectKeywordsValidatorFactory.objectKeywordsValidator;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.DEFAULT_VALIDATOR_FACTORY;
import static io.dugnutt.jsonschema.validator.ValidationMocks.createTestValidator;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockBooleanSchema;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockNullSchema;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockNumberSchema;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockObjectSchema;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockSchema;
import static io.dugnutt.jsonschema.validator.ValidationMocks.mockStringSchema;
import static io.dugnutt.jsonschema.validator.ValidationMocks.pathAware;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.buildWithLocation;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.countCauseByJsonPointer;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.countMatchingMessage;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.expectFailure;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.failureOf;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.verifyFailure;
import static javax.json.spi.JsonProvider.provider;
import static org.junit.Assert.assertEquals;

public class ObjectKeywordsValidatorTest {

    private static final JsonObject OBJECTS = JsonUtils.readResourceAsJson("/io/dugnutt/jsonschema/six/objecttestcases.json", JsonObject.class);

    @Test
    public void additionalPropertiesOnEmptyObject() {

        final JsonObject input = OBJECTS.getJsonObject("emptyObject");
        final Schema testSchema = mockObjectSchema()
                .schemaOfAdditionalProperties(mockBooleanSchema())
                .build();

        expectSuccess(() -> objectKeywordsValidator().forSchema(testSchema, DEFAULT_VALIDATOR_FACTORY).validate(pathAware(input)));
    }

    @Test
    public void additionalPropertySchema() {
        String expectedSchemaLocation = "#/bool/location";
        JsonSchemaBuilder boolSchema = jsonSchemaBuilderWithId(expectedSchemaLocation).type(BOOLEAN);
        Schema schema = mockObjectSchema().schemaOfAdditionalProperties(boolSchema).build();
        failureOf(schema)
                .input(OBJECTS.get("additionalPropertySchema"))
                .expected(error -> {
                    //Other stuff
                    assertEquals(1, error.getCauses().size());
                    final ValidationError cause = error.getCauses().get(0);
                    assertEquals(expectedSchemaLocation, cause.getSchemaLocation().toString());
                    assertEquals(TYPE, cause.getKeyword());
                    assertEquals(boolSchema.build(), cause.getViolatedSchema());
                })
                .expectedPointer("#")
                .expectedKeyword(ADDITIONAL_PROPERTIES)
                .expectedSchemaLocation("#")
                .expect();
    }

    @Test
    public void maxPropertiesFailure() {
        Schema subject = buildWithLocation(mockObjectSchema().maxProperties(2));
        failureOf(subject)
                .input(OBJECTS.get("maxPropertiesFailure"))
                .expectedPointer("#")
                .expectedKeyword("maxProperties")
                .expect();
    }

    @Test
    public void minPropertiesFailure() {
        Schema subject = buildWithLocation(mockObjectSchema().minProperties(2));
        failureOf(subject)
                .input(OBJECTS.get("minPropertiesFailure"))
                .expectedPointer("#")
                .expectedKeyword("minProperties")
                .expect();
    }

    @Test
    public void multipleAdditionalProperties() {
        Schema subject = mockObjectSchema()
                .schemaOfAdditionalProperties(mockStringSchema())
                .build();

        final SchemaValidator testValidator = createTestValidator(subject);
        ValidationError error = verifyFailure(() -> testValidator.validate(readJsonObject("{\"a\":true,\"b\":true}")));

        assertEquals("#: Additional properties were invalid", error.getMessage());
        assertEquals(ADDITIONAL_PROPERTIES, error.getKeyword());
        assertEquals(2, error.getCauses().size());
    }

    @Test
    public void multipleSchemaDepViolation() {
        JsonSchemaBuilder billingAddressSchema = mockStringSchema();
        JsonSchemaBuilder billingNameSchema = mockStringSchema().minLength(4);
        Schema subject = mockObjectSchema()
                .propertySchema("name", mockStringSchema())
                .propertySchema("credit_card", mockNumberSchema())
                .schemaDependency("credit_card", mockObjectSchema()
                        .propertySchema("billing_address", billingAddressSchema)
                        .requiredProperty("billing_address")
                        .propertySchema("billing_name", billingNameSchema))
                .schemaDependency("name", mockObjectSchema()
                        .requiredProperty("age"))
                .build();

        ValidationError e = verifyFailure(() -> createTestValidator(subject).validate(OBJECTS.get("schemaDepViolation")));
        ValidationError creditCardFailure = e.getCauses().get(0);
        ValidationError ageFailure = e.getCauses().get(1);
        // due to schemaDeps being stored in (unsorted) HashMap, the exceptions may need to be swapped
        if (creditCardFailure.getCauses().isEmpty()) {
            ValidationError tmp = creditCardFailure;
            creditCardFailure = ageFailure;
            ageFailure = tmp;
        }
        ValidationError billingAddressFailure = creditCardFailure.getCauses().get(0);
        assertEquals("#/billing_address", billingAddressFailure.getPointerToViolation());
        assertEquals(billingAddressSchema, billingAddressFailure.getViolatedSchema());
        ValidationError billingNameFailure = creditCardFailure
                .getCauses().get(1);
        assertEquals("#/billing_name", billingNameFailure.getPointerToViolation());
        assertEquals(billingNameSchema, billingNameFailure.getViolatedSchema());
        assertEquals("#", ageFailure.getPointerToViolation());
        assertEquals("#: required key [age] not found", ageFailure.getMessage());
    }

    @Test
    public void multipleViolations() {
        Schema subject = mockObjectSchema()
                .propertySchema("numberProp", mockNumberSchema())
                .patternProperty("^string.*", mockStringSchema())
                .propertySchema("boolProp", mockBooleanSchema())
                .requiredProperty("boolProp")
                .build();

        var e = verifyFailure(() -> createTestValidator(subject).validate(OBJECTS.get("multipleViolations")));

        assertEquals(3, e.getCauses().size());
        assertEquals(1, countCauseByJsonPointer(e, "#"));
        assertEquals(1, countCauseByJsonPointer(e, "#/numberProp"));
        assertEquals(1, countCauseByJsonPointer(e, "#/stringPatternMatch"));

        List<String> messages = e.getAllMessages();
        assertEquals(3, messages.size());
        assertEquals(1, countMatchingMessage(messages, "#:"));
        assertEquals(1, countMatchingMessage(messages, "#/numberProp:"));
        assertEquals(1, countMatchingMessage(messages, "#/stringPatternMatch:"));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void multipleViolationsNested() throws Exception {
        Callable<JsonSchemaBuilder> newBuilder = () -> mockObjectSchema()
                .propertySchema("numberProp", mockNumberSchema())
                .patternProperty("^string.*", mockStringSchema())
                .propertySchema("boolProp", mockBooleanSchema())
                .requiredProperty("boolProp");

        JsonSchemaBuilder nested2 = newBuilder.call();
        JsonSchemaBuilder nested1 = newBuilder.call().propertySchema("nested", nested2);
        Schema subject = newBuilder.call().propertySchema("nested", nested1).build();

        ValidationError subjectException = verifyFailure(() -> createTestValidator(subject).validate(OBJECTS.get("multipleViolationsNested")));

        assertEquals("#: 9 schema violations found", subjectException.getMessage());
        assertEquals(4, subjectException.getCauses().size());
        assertEquals(1, countCauseByJsonPointer(subjectException, "#"));
        assertEquals(1, countCauseByJsonPointer(subjectException, "#/numberProp"));
        assertEquals(1, countCauseByJsonPointer(subjectException, "#/stringPatternMatch"));
        assertEquals(1, countCauseByJsonPointer(subjectException, "#/nested"));

        ValidationError nested1Exception = subjectException.getCauses().stream()
                .filter(ex -> ex.getPointerToViolation().equals("#/nested"))
                .findFirst()
                .get();
        assertEquals("#/nested: 6 schema violations found", nested1Exception.getMessage());
        assertEquals(4, nested1Exception.getCauses().size());
        assertEquals(1, countCauseByJsonPointer(nested1Exception, "#/nested"));
        assertEquals(1, countCauseByJsonPointer(nested1Exception, "#/nested/numberProp"));
        assertEquals(1, countCauseByJsonPointer(nested1Exception, "#/nested/stringPatternMatch"));
        assertEquals(1, countCauseByJsonPointer(nested1Exception, "#/nested/nested"));

        ValidationError nested2Exception = nested1Exception.getCauses().stream()
                .filter(ex -> ex.getPointerToViolation().equals("#/nested/nested"))
                .findFirst()
                .get();
        assertEquals("#/nested/nested: 3 schema violations found", nested2Exception.getMessage());
        assertEquals(3, nested2Exception.getCauses().size());
        assertEquals(1, countCauseByJsonPointer(nested2Exception, "#/nested/nested"));
        assertEquals(1, countCauseByJsonPointer(nested2Exception, "#/nested/nested/numberProp"));
        assertEquals(1, countCauseByJsonPointer(nested2Exception, "#/nested/nested/stringPatternMatch"));

        List<String> messages = subjectException.getAllMessages();
        assertEquals(9, messages.size());
        assertEquals(1, countMatchingMessage(messages, "#:"));
        assertEquals(1, countMatchingMessage(messages, "#/numberProp:"));
        assertEquals(1, countMatchingMessage(messages, "#/stringPatternMatch:"));
        assertEquals(1, countMatchingMessage(messages, "#/nested:"));
        assertEquals(1, countMatchingMessage(messages, "#/nested/numberProp:"));
        assertEquals(1, countMatchingMessage(messages, "#/nested/stringPatternMatch:"));
        assertEquals(1, countMatchingMessage(messages, "#/nested/nested:"));
        assertEquals(1, countMatchingMessage(messages, "#/nested/nested/numberProp:"));
        assertEquals(1, countMatchingMessage(messages, "#/nested/nested/stringPatternMatch:"));
    }

    @Test
    public void noProperties() {
        expectSuccess(() -> createTestValidator(mockObjectSchema().build()).validate(OBJECTS.get("noProperties")));
    }

    @Test
    public void notRequireObject() {
        expectSuccess(() -> {
            final Schema objectSchema = mockSchema().build();
            return createTestValidator(objectSchema).validate(jsonStringValue("foo"));
        });
    }

    @Test
    public void patternPropertyOnEmptyObjct() {
        final Schema schema = mockObjectSchema()
                .patternProperty("b_.*", mockBooleanSchema())
                .build();
        expectSuccess(() -> createTestValidator(schema).validate(blankJsonObject()));
    }

    @Test
    public void patternPropertyOverridesAdditionalPropSchema() {
        final Schema schema = mockObjectSchema()
                .schemaOfAdditionalProperties(mockNumberSchema())
                .patternProperty("aa.*", mockBooleanSchema())
                .build();
        expectSuccess(() -> createTestValidator(schema).validate(OBJECTS.get("patternPropertyOverridesAdditionalPropSchema")));
    }

    @Test
    public void patternPropertyViolation() {
        Schema subject = mockObjectSchema()
                .patternProperty("^b_.*", mockBooleanSchema())
                .patternProperty("^s_.*", mockStringSchema())
                .build();
        expectFailure(subject, mockBooleanSchema().build(), "#/b_1",
                OBJECTS.get("patternPropertyViolation"));
    }

    @Test
    public void patternPropsOverrideAdditionalProps() {
        final Schema schema = mockObjectSchema()
                .patternProperty("^v.*", mockSchema())
                .schemaOfAdditionalProperties(
                        mockBooleanSchema().constValue(JsonValue.FALSE)
                )
                .build();
        expectSuccess(() -> createTestValidator(schema).validate(OBJECTS.get("patternPropsOverrideAdditionalProps")));
    }

    @Test
    public void propertyDepViolation() {
        mockObjectSchema()
                .propertySchema("ifPresent", mockNullSchema())
                .propertySchema("mustBePresent", mockBooleanSchema());
        Schema subject =
                mockObjectSchema()
                        .propertySchema("ifPresent", mockNullSchema())
                        .propertyDependency("ifPresent", "mustBePresent").build();

        failureOf(subject)
                .input(OBJECTS.get("propertyDepViolation"))
                .expectedKeyword("dependencies")
                .expect();
    }

    @Test
    public void propertyNameSchemaSchemaViolation() {
        final JsonSchemaBuilder propertyNameSchema = mockStringSchema().pattern("^[a-z_]{3,8}$");
        Schema subject = mockObjectSchema()
                .propertyNameSchema(propertyNameSchema)
                .id("#")
                .build();
        failureOf(subject)
                .input(OBJECTS.getJsonObject("propertyNameSchemaViolation"))
                .expected(error -> {
                    Assert.assertEquals("#", error.getSchemaLocation().toString());
                    Assert.assertEquals(3, error.getViolationCount());
                })
                .expectedKeyword(JsonSchemaKeyword.PROPERTY_NAMES.key())
                .expectedSchemaLocation("#")
                .expect();
    }

    @Test
    public void propertySchemaViolation() {
        Schema subject = mockObjectSchema()
                .propertySchema("boolProp", mockBooleanSchema())
                .build();
        expectFailure(subject, mockBooleanSchema().build(), "#/boolProp",
                OBJECTS.get("propertySchemaViolation"));
    }

    @Test
    public void requireObject() {
        expectFailure(mockObjectSchema().build(), "#", jsonStringValue("foo"));
    }

    @Test
    public void requiredProperties() {
        final Schema subject = mockObjectSchema().propertySchema("boolProp", mockBooleanSchema())
                .propertySchema("nullProp", mockNullSchema())
                .requiredProperty("boolProp")
                .build();

        failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("required")
                .input(OBJECTS.get("requiredProperties"))
                .expect();
    }

    @Test
    public void schemaDepViolation() {
        JsonSchemaBuilder billingAddressSchema = mockStringSchema();

        final JsonSchemaBuilder schemaBuilder = mockObjectSchema();
        schemaBuilder.propertySchema("billing_address", billingAddressSchema);
        schemaBuilder.propertySchema("name", mockStringSchema());
        schemaBuilder.propertySchema("credit_card", mockNumberSchema());
        Schema subject = schemaBuilder
                .schemaDependency("credit_card", mockObjectSchema()
                        .requiredProperty("billing_address"))
                .build();
        expectFailure(subject, billingAddressSchema.build(), "#/billing_address",
                OBJECTS.get("schemaDepViolation"));
    }

    @Test
    public void schemaPointerIsPassedToValidationError() {
        Schema subject = mockObjectSchema()
                .id("#/dependencies/a")
                .minProperties(1)
                .build();
        ValidationError e = verifyFailure(() -> createTestValidator(subject).validate(provider().createValue(1)));
        assertEquals("#/dependencies/a", e.getSchemaLocation());
    }

    @Test
    public void testImmutability() {
        JsonSchemaBuilder builder = mockObjectSchema();
        builder.propertyDependency("a", "b");
        builder.schemaDependency("a", mockBooleanSchema());
        builder.patternProperty("aaa", mockBooleanSchema());
        Schema schema = builder.build();
        builder.propertyDependency("c", "a");
        builder.schemaDependency("b", mockBooleanSchema());
        builder.patternProperty("bbb", mockBooleanSchema());
        final ObjectKeywords keywords = schema.getObjectKeywords().get();
        assertEquals(1, keywords.getPropertyDependencies().size());
        assertEquals(1, keywords.getSchemaDependencies().size());
        assertEquals(1, keywords.getPatternProperties().size());
    }

    @Test
    public void toStringNoAdditionalProperties() {
        JsonObject rawSchemaJson = readResourceAsJson("tostring/objectschema.json", JsonObject.class);
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void toStringNoExplicitType() {
        JsonObject rawSchemaJson = readResourceAsJson("tostring/objectschema.json", JsonObject.class);
        rawSchemaJson = provider().createObjectBuilder(rawSchemaJson)
                .remove("type")
                .build();
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void toStringSchemaDependencies() {
        JsonObject rawSchemaJson = readResourceAsJson("tostring/objectschema-schemadep.json", JsonObject.class);
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void toStringTest() {
        JsonObject rawSchemaJson = readResourceAsJson("tostring/objectschema.json", JsonObject.class);
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void typeFailure() {
        failureOf(mockObjectSchema().build())
                .expectedKeyword("type")
                .input("a")
                .expect();
    }

    JsonObject readResourceAsJson(String url, Class<JsonObject> clazz) {
        return JsonUtils.readResourceAsJson("/io/dugnutt/jsonschema/six/" + url, clazz);
    }
}
