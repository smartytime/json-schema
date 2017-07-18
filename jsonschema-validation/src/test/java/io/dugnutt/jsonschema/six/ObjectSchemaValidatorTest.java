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
import io.dugnutt.jsonschema.validator.ObjectSchemaValidator;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationError;
import lombok.experimental.var;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import javax.json.JsonPointer;
import java.util.List;
import java.util.concurrent.Callable;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.buildValidatorWithLocation;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.buildWithLocation;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.countCauseByJsonPointer;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.countMatchingMessage;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectFailure;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.failureOf;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.verifyFailure;
import static io.dugnutt.jsonschema.utils.JsonUtils.blankJsonObject;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.utils.JsonUtils.readJsonObject;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;
import static javax.json.spi.JsonProvider.provider;
import static org.junit.Assert.assertEquals;

public class ObjectSchemaValidatorTest {

    private static final JsonObject OBJECTS = JsonUtils.readResourceAsJson("/io/dugnutt/jsonschema/six/objecttestcases.json", JsonObject.class);

    @Test
    public void additionalPropertiesOnEmptyObject() {
        expectSuccess(() -> new ObjectSchemaValidator(ObjectSchema.builder()
                .schemaOfAdditionalProperties(BooleanSchema.BOOLEAN_SCHEMA).build())
                .validate(OBJECTS.getJsonObject("emptyObject")));
    }

    @Test
    public void additionalPropertySchema() {
        String expectedSchemaLocation = "#/bool/location";
        BooleanSchema boolSchema = BooleanSchema.builder().schemaLocation(expectedSchemaLocation).build();
        ObjectSchema subject = buildWithLocation(ObjectSchema.builder()
                .schemaOfAdditionalProperties(boolSchema));
        failureOf(subject)
                .input(OBJECTS.get("additionalPropertySchema"))
                .expectedPointer("#/foo")
                .expectedSchemaLocation(expectedSchemaLocation)
                .expect();
        expectFailure(subject, "#/foo", OBJECTS.get("additionalPropertySchema"));
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(ObjectSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void maxPropertiesFailure() {
        ObjectSchema subject = buildWithLocation(ObjectSchema.builder().maxProperties(2));
        failureOf(subject)
                .input(OBJECTS.get("maxPropertiesFailure"))
                .expectedPointer("#")
                .expectedKeyword("maxProperties")
                .expect();
    }

    @Test
    public void minPropertiesFailure() {
        ObjectSchema subject = buildWithLocation(ObjectSchema.builder().minProperties(2));
        failureOf(subject)
                .input(OBJECTS.get("minPropertiesFailure"))
                .expectedPointer("#")
                .expectedKeyword("minProperties")
                .expect();
    }

    @Test
    public void multipleAdditionalProperties() {
        SchemaValidator<?> subject = buildValidatorWithLocation(ObjectSchema.builder().additionalProperties(false));
        var error = verifyFailure(() -> subject.validate(readJsonObject("{\"a\":true,\"b\":true}")));

        assertEquals("#: 2 schema violations found", error.getMessage());
        assertEquals(2, error.getCauses().size());
    }

    @Test
    public void multipleSchemaDepViolation() {
        Schema billingAddressSchema = new StringSchema();
        Schema billingNameSchema = StringSchema.builder().minLength(4).build();
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("name", new StringSchema())
                .addPropertySchema("credit_card", NumberSchema.builder().build())
                .schemaDependency("credit_card", ObjectSchema.builder()
                        .addPropertySchema("billing_address", billingAddressSchema)
                        .addRequiredProperty("billing_address")
                        .addPropertySchema("billing_name", billingNameSchema)
                        .build())
                .schemaDependency("name", ObjectSchema.builder()
                        .addRequiredProperty("age")
                        .build())
                .build();

        var e = verifyFailure(() -> createValidatorForSchema(subject).validate(OBJECTS.get("schemaDepViolation")));
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
        Schema subject = ObjectSchema.builder()
                .addPropertySchema("numberProp", new NumberSchema())
                .patternProperty("^string.*", new StringSchema())
                .addPropertySchema("boolProp", BooleanSchema.BOOLEAN_SCHEMA)
                .addRequiredProperty("boolProp")
                .build();

        var e = verifyFailure(() -> createValidatorForSchema(subject).validate(OBJECTS.get("multipleViolations")));
        ;
        Assert.fail("did not throw exception for 3 schema violations");

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
        Callable<ObjectSchema.Builder> newBuilder = () -> ObjectSchema.builder()
                .addPropertySchema("numberProp", new NumberSchema())
                .patternProperty("^string.*", new StringSchema())
                .addPropertySchema("boolProp", BooleanSchema.BOOLEAN_SCHEMA)
                .addRequiredProperty("boolProp");

        Schema nested2 = newBuilder.call().build();
        Schema nested1 = newBuilder.call().addPropertySchema("nested", nested2).build();
        Schema subject = newBuilder.call().addPropertySchema("nested", nested1).build();

        var subjectException = verifyFailure(() -> createValidatorForSchema(subject).validate(OBJECTS.get("multipleViolationsNested")));
        ;
        Assert.fail("did not throw exception for 9 schema violations");

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
    public void noAdditionalProperties() {
        ObjectSchema subject = ObjectSchema.builder().additionalProperties(false).build();
        expectFailure(subject, "#", OBJECTS.get("propertySchemaViolation"));
    }

    @Test
    public void noProperties() {
        expectSuccess(() -> createValidatorForSchema(ObjectSchema.builder().build()).validate(OBJECTS.get("noProperties")));
        ;
    }

    @Test
    public void notRequireObject() {
        expectSuccess(() -> {
            final ObjectSchema objectSchema = ObjectSchema.builder().requiresObject(false).build();
            return createValidatorForSchema(objectSchema).validate(jsonStringValue("foo"));
        });
        ;
    }

    @Test
    public void patternPropertyOnEmptyObjct() {
        final ObjectSchema schema = ObjectSchema.builder()
                .patternProperty("b_.*", BooleanSchema.BOOLEAN_SCHEMA)
                .build();
        expectSuccess(() -> createValidatorForSchema(schema).validate(blankJsonObject()));
        ;
    }

    @Test
    public void patternPropertyOverridesAdditionalPropSchema() {
        final ObjectSchema schema = ObjectSchema.builder()
                .schemaOfAdditionalProperties(new NumberSchema())
                .patternProperty("aa.*", BooleanSchema.BOOLEAN_SCHEMA)
                .build();
        expectSuccess(() -> createValidatorForSchema(schema).validate(OBJECTS.get("patternPropertyOverridesAdditionalPropSchema")));
        ;
    }

    @Test
    public void patternPropertyViolation() {
        ObjectSchema subject = ObjectSchema.builder()
                .patternProperty("^b_.*", BooleanSchema.BOOLEAN_SCHEMA)
                .patternProperty("^s_.*", new StringSchema())
                .build();
        expectFailure(subject, BooleanSchema.BOOLEAN_SCHEMA, "#/b_1",
                OBJECTS.get("patternPropertyViolation"));
    }

    @Test
    public void patternPropsOverrideAdditionalProps() {
        final ObjectSchema schema = ObjectSchema.builder()
                .patternProperty("^v.*", EmptySchema.EMPTY_SCHEMA)
                .additionalProperties(false)
                .build();
        expectSuccess(() -> createValidatorForSchema(schema).validate(OBJECTS.get("patternPropsOverrideAdditionalProps")));
        ;
    }

    @Test
    public void propertyDepViolation() {
        ObjectSchema subject = buildWithLocation(
                ObjectSchema.builder()
                        .addPropertySchema("ifPresent", NullSchema.INSTANCE)
                        .addPropertySchema("mustBePresent", BooleanSchema.BOOLEAN_SCHEMA)
                        .propertyDependency("ifPresent", "mustBePresent")
        );
        failureOf(subject)
                .input(OBJECTS.get("propertyDepViolation"))
                .expectedKeyword("dependencies")
                .expect();
    }

    @Test
    public void propertySchemaViolation() {
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("boolProp", BooleanSchema.BOOLEAN_SCHEMA).build();
        expectFailure(subject, BooleanSchema.BOOLEAN_SCHEMA, "#/boolProp",
                OBJECTS.get("propertySchemaViolation"));
    }

    @Test
    public void requireObject() {
        expectFailure(buildWithLocation(ObjectSchema.builder()), "#", jsonStringValue("foo"));
    }

    @Test
    public void requiredProperties() {
        ObjectSchema subject = buildWithLocation(
                ObjectSchema.builder()
                        .addPropertySchema("boolProp", BooleanSchema.BOOLEAN_SCHEMA)
                        .addPropertySchema("nullProp", NullSchema.INSTANCE)
                        .addRequiredProperty("boolProp")
        );
        failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("required")
                .input(OBJECTS.get("requiredProperties"))
                .expect();
    }

    @Test
    public void schemaDepViolation() {
        Schema billingAddressSchema = new StringSchema();
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("name", new StringSchema())
                .addPropertySchema("credit_card", NumberSchema.builder().build())
                .schemaDependency("credit_card", ObjectSchema.builder()
                        .addPropertySchema("billing_address", billingAddressSchema)
                        .addRequiredProperty("billing_address")
                        .build())
                .build();
        expectFailure(subject, billingAddressSchema, "#/billing_address",
                OBJECTS.get("schemaDepViolation"));
    }

    @Test(expected = SchemaException.class)
    public void schemaForNoAdditionalProperties() {
        ObjectSchema.builder().additionalProperties(false)
                .schemaOfAdditionalProperties(BooleanSchema.BOOLEAN_SCHEMA).build();
    }

    @Test
    public void schemaPointerIsPassedToValidationError() {
        JsonPointer pointer = provider().createPointer("/dependencies/a");
        Schema subject = ObjectSchema.builder().requiresObject(true)
                .minProperties(1)
                .schemaLocation("#/dependencies/a").build();
        var e = verifyFailure(() -> createValidatorForSchema(subject).validate(provider().createValue(1)));
        assertEquals("#/dependencies/a", e.getSchemaLocation());
    }

    @Test
    public void testImmutability() {
        ObjectSchema.Builder builder = ObjectSchema.builder();
        builder.propertyDependency("a", "b");
        builder.schemaDependency("a", BooleanSchema.BOOLEAN_SCHEMA);
        builder.patternProperty("aaa", BooleanSchema.BOOLEAN_SCHEMA);
        ObjectSchema schema = builder.build();
        builder.propertyDependency("c", "a");
        builder.schemaDependency("b", BooleanSchema.BOOLEAN_SCHEMA);
        builder.patternProperty("bbb", BooleanSchema.BOOLEAN_SCHEMA);
        assertEquals(1, schema.getPropertyDependencies().size());
        assertEquals(1, schema.getSchemaDependencies().size());
        assertEquals(1, schema.getPatternProperties().size());
    }

    @Test
    public void toStringNoAdditionalProperties() {
        JsonObject rawSchemaJson = readResourceAsJson("tostring/objectschema.json", JsonObject.class);
        rawSchemaJson = provider().createObjectBuilder(rawSchemaJson)
                .add("additionalProperties", false)
                .build();
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
        failureOf(ObjectSchema.builder())
                .expectedKeyword("type")
                .input("a")
                .expect();
    }

    JsonObject readResourceAsJson(String url, Class<JsonObject> clazz) {
        return JsonUtils.readResourceAsJson("/io/dugnutt/jsonschema/six/" + url, clazz);
    }
}
