package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.util.Optional;

import static io.sbsp.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.sbsp.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.sbsp.jsonschema.utils.JsonUtils.blankJsonArray;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonArray;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonObjectBuilder;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.sbsp.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.createTestValidator;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockSchema;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.expectSuccess;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.failureOf;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.verifyFailure;
import static javax.json.spi.JsonProvider.provider;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseSchemaValidatorEnumTest {
    private JsonArrayBuilder possibleValues;

    @Before
    public void before() {
        possibleValues = provider().createArrayBuilder()
                .add(JsonValue.TRUE)
                .add("foo");
    }

    @Test
    public void failure() {
        failureOf(subject())
                .expectedPointer("#")
                .expectedKeyword(JsonSchemaKeyword.ENUM)
                .input(jsonArray(1))
                .expect();
    }

    @Test
    public void objectInArrayMatches() {
        JsonArray possibleValues = this.possibleValues
                .add(jsonObjectBuilder()
                        .add("a", true)
                        .build())
                .build();

        Schema subject = subject().enumValues(possibleValues).build();

        JsonObject testValues = jsonObjectBuilder()
                .add("a", true)
                .build();
        expectSuccess(() -> {
            Optional<ValidationError> error = createTestValidator(subject).validate(testValues);
            return error;
        });
    }

    @Test
    public void success() {
        possibleValues.add(blankJsonArray());
        final JsonValue validJsonObject = JsonUtils.readValue("{\"a\" : 0}");
        possibleValues.add(validJsonObject);
        Schema schema = subject().build();
        SchemaValidator subject = createTestValidator(schema);

        expectSuccess(() -> subject.validate(JsonValue.TRUE));
        expectSuccess(() -> subject.validate(jsonStringValue("foo")));
        expectSuccess(() -> subject.validate(blankJsonArray()));
        expectSuccess(() -> subject.validate(validJsonObject));
    }

    @Test
    public void toStringTest() {
        String toString = subject().build().toString();
        JsonObject actual = JsonUtils.readJsonObject(toString);
        Assert.assertEquals(1, actual.keySet().size());
        JsonArray pv = jsonArray(true, "foo");
        Assert.assertEquals(pv, actual.getJsonArray("enum"));
    }

    @Test
    public void validate_WhenNumbersHaveDifferentLexicalValues_EnumDoesntMatch() {
        JsonArray testEnum = JsonUtils.readValue("[1, 1.0, 1.00]", JsonArray.class);
        JsonNumber testValNotSame = JsonUtils.readValue("1.000", JsonNumber.class);

        final Schema schema = jsonSchemaBuilder().enumValues(testEnum).build();

        final Optional<ValidationError> validate = createTestValidator(schema).validate(testValNotSame);

        assertTrue("Should have an error", validate.isPresent());
        Assert.assertEquals("Should be for enum keyword", JsonSchemaKeyword.ENUM, validate.get().getKeyword());
    }

    @Test
    public void validate_WhenNumbersHaveSameLexicalValues_EnumMatches() {
        JsonArray testEnum = JsonUtils.readValue("[1, 1.0, 1.00]", JsonArray.class);
        JsonNumber testValNotSame = JsonUtils.readValue("1.00", JsonNumber.class);

        final Schema schema = mockSchema().enumValues(testEnum).build();
        final Optional<ValidationError> validate = createTestValidator(schema).validate(testValNotSame);

        assertFalse("Should not an error", validate.isPresent());
    }

    @Test
    public void validate_WhenSubjectIsArray_AndEnumIsAppliedToTheArray_AndArrayMatchesItemsInOrder_ThenTheArrayValidates() {
        // To validate you either need to be:
        // An array with items [true, "foo", {"a": true}], OR
        // The number literal 42
        JsonArray possibleValuesContainer = JsonProvider.provider()
                .createArrayBuilder()
                .add(
                        this.possibleValues
                                .add(jsonObjectBuilder()
                                        .add("a", true)
                                ))
                .add(42).build();

        Schema subject = subject().enumValues(possibleValuesContainer).build();
        JsonArray testValues = provider().createArrayBuilder()
                .add(JsonValue.TRUE)
                .add("foo")
                .add(jsonObjectBuilder()
                        .add("a", true)
                        .build())
                .build();

        expectSuccess(() -> {
            Optional<ValidationError> error = createValidatorForSchema(subject).validate(testValues);
            return error;
        });
    }

    @Test
    public void validate_WhenSubjectIsArray_AndEnumIsAppliedToTheArray_ThenTheArrayFailsToValidate() {
        JsonArray possibleValues = this.possibleValues
                .add(jsonObjectBuilder()
                        .add("a", true)
                        .build())
                .build();

        Schema subject = subject().enumValues(possibleValues).build();

        JsonArray testValues = provider().createArrayBuilder()
                .add(jsonObjectBuilder()
                        .add("a", true)
                        .build())
                .build();

        verifyFailure(() -> {
            Optional<ValidationError> error = createValidatorForSchema(subject).validate(testValues);
            return error;
        });
    }

    private JsonSchemaBuilder subject() {
        return jsonSchemaBuilder().enumValues(possibleValues.build());
    }
}
