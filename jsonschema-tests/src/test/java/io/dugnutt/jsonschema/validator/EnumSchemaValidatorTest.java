package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.EnumSchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnumSchemaValidatorTest {

    @Test
    public void validate_WhenNumbersHaveDifferentLexicalValues_EnumDoesntMatch() {
        JsonArray testEnum = JsonUtils.readValue("[1, 1.0, 1.00]", JsonArray.class);
        JsonNumber testValNotSame = JsonUtils.readValue("1.000", JsonNumber.class);

        final EnumSchema schema = new EnumSchema(new EnumSchema.Builder(SchemaLocation.schemaLocation()).possibleValues(testEnum));
        final EnumSchemaValidator validator = new EnumSchemaValidator(schema);

        final Optional<ValidationError> validate = validator.validate(testValNotSame);

        assertTrue("Should have an error", validate.isPresent());
        Assert.assertEquals("Should be for enum keyword", JsonSchemaKeyword.ENUM.key(), validate.get().getKeyword().name());
    }

    @Test
    public void validate_WhenNumbersHaveSameLexicalValues_EnumMatches() {
        JsonArray testEnum = JsonUtils.readValue("[1, 1.0, 1.00]", JsonArray.class);
        JsonNumber testValNotSame = JsonUtils.readValue("1.00", JsonNumber.class);

        final EnumSchema schema = new EnumSchema(new EnumSchema.Builder(SchemaLocation.schemaLocation()).possibleValues(testEnum));
        final EnumSchemaValidator validator = new EnumSchemaValidator(schema);

        final Optional<ValidationError> validate = validator.validate(testValNotSame);

        assertFalse("Should not an error", validate.isPresent());
    }
}