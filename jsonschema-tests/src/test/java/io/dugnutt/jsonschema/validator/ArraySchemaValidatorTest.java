package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import java.util.Optional;

public class ArraySchemaValidatorTest {

    @Test
    public void validate_WhenEqualNumbersWithDifferentLexicalRepresentations_ThenUnique() {
        final ArraySchema arraySchema = ArraySchema.builder()
                .uniqueItems(true)
                .requiresArray(true)
                .build();
        final ArraySchemaValidator validator = new ArraySchemaValidator(arraySchema);
        final Optional<ValidationError> errors = validator.validate(JsonUtils.readValue("[1.0, 1, 1.00]", JsonArray.class));
        Assert.assertFalse("Should have no errors", errors.isPresent());
    }

    @Test
    public void validate_WhenEqualNumbersWithSameLexicalRepresentations_ThenNotUnique() {
        final ArraySchema arraySchema = ArraySchema.builder().uniqueItems(true).requiresArray(true).build();
        final ArraySchemaValidator validator = new ArraySchemaValidator(arraySchema);
        final Optional<ValidationError> errors = validator.validate(JsonUtils.readValue("[1.0, 1.0, 1.00]", JsonArray.class));
        Assert.assertTrue("Should have no errors", errors.isPresent());
        Assert.assertEquals("Should have no errors", "uniqueItems", errors.get().getKeyword());

    }

}