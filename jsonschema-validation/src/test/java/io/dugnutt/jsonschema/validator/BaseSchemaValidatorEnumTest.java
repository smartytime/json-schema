package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.EmptySchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.failureOf;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.verifyFailure;
import static io.dugnutt.jsonschema.utils.JsonUtils.blankJsonArray;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonArray;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonObjectBuilder;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.utils.JsonUtils.readValue;
import static io.dugnutt.jsonschema.validator.ValidationMocks.alwaysSuccessfulValidator;
import static io.dugnutt.jsonschema.validator.ValidationMocks.vsubject;
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
                .expectedKeyword("enum")
                .input(readValue("[1]"))
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
            Optional<ValidationError> error = SchemaValidatorFactory.createValidatorForSchema(subject).validate(testValues);
            return error;
        });
    }

    @Test
    public void success() {
        possibleValues.add(blankJsonArray());
        final JsonValue validJsonObject = JsonUtils.readValue("{\"a\" : 0}");
        possibleValues.add(validJsonObject);
        BaseSchemaValidator<?> subject = new BaseSchemaValidator(subject().build(), alwaysSuccessfulValidator());
        expectSuccess(() -> subject.validate(vsubject(JsonValue.TRUE)));
        expectSuccess(() -> subject.validate(vsubject(jsonStringValue("foo"))));
        expectSuccess(() -> subject.validate(vsubject(blankJsonArray())));
        expectSuccess(() -> subject.validate(vsubject(validJsonObject)));
    }

    @Test
    public void toStringTest() {
        String toString = subject().build().toString();
        JsonObject actual = JsonUtils.readJsonObject(toString);
        Assert.assertEquals(1, actual.keySet().size());
        JsonArray pv = jsonArray(true, "foo");
        Assert.assertEquals(asSet(pv), asSet(actual.getJsonArray("enum")));
    }

    @Test
    public void validate_WhenNumbersHaveDifferentLexicalValues_EnumDoesntMatch() {
        JsonArray testEnum = JsonUtils.readValue("[1, 1.0, 1.00]", JsonArray.class);
        JsonNumber testValNotSame = JsonUtils.readValue("1.000", JsonNumber.class);

        final Schema schema = EmptySchema.builder().enumValues(testEnum).build();
        final SchemaValidator<?> validator = new BaseSchemaValidator<>(schema, alwaysSuccessfulValidator());

        final Optional<ValidationError> validate = validator.validate(testValNotSame);

        assertTrue("Should have an error", validate.isPresent());
        Assert.assertEquals("Should be for enum keyword", JsonSchemaKeyword.ENUM, validate.get().getKeyword());
    }

    @Test
    public void validate_WhenNumbersHaveSameLexicalValues_EnumMatches() {
        JsonArray testEnum = JsonUtils.readValue("[1, 1.0, 1.00]", JsonArray.class);
        JsonNumber testValNotSame = JsonUtils.readValue("1.00", JsonNumber.class);

        final Schema schema = EmptySchema.builder().enumValues(testEnum).build();
        final SchemaValidator<?> validator = new BaseSchemaValidator<>(schema, alwaysSuccessfulValidator());

        final Optional<ValidationError> validate = validator.validate(testValNotSame);

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
            Optional<ValidationError> error = SchemaValidatorFactory.createValidatorForSchema(subject).validate(testValues);
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
            Optional<ValidationError> error = SchemaValidatorFactory.createValidatorForSchema(subject).validate(testValues);
            return error;
        });
    }

    private Schema.Builder subject() {
        return EmptySchema.builder()
                .enumValues(possibleValues.build());
    }

    private Set<Object> asSet(final JsonArray array) {
        return new HashSet<>(JsonUtils.extractArray(array));
    }
}
