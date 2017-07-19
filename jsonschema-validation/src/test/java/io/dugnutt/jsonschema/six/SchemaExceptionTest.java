package io.dugnutt.jsonschema.six;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static io.dugnutt.jsonschema.six.SchemaLocation.schemaLocation;
import static io.dugnutt.jsonschema.validator.SchemaValidator.failure;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class SchemaExceptionTest {

    private static final NullSchema SCHEMA = NullSchema.builder(SchemaLocation.schemaLocation())
            .build();

    @Rule
    public final ExpectedException expExc = ExpectedException.none();

    @Test
    public void nullActual() {
        NullSchema schema = NullSchema.builder(schemaLocation("#/required/2")).build();
        String actual = failure(schema, JsonSchemaType.NULL, JsonSchemaType.STRING).getErrorMessage();
        assertEquals("#/required/2: expected type: String, found: null", actual);
    }

    @Test
    public void nullJSONPointer() {
        expExc.expect(NullPointerException.class);
        expExc.expectMessage("pointer cannot be null");
        failure(null, JsonSchemaType.NUMBER, JsonSchemaType.STRING);
    }

    @Test
    public void testBuildMessageSingleExcType() {
        String actual = failure(SCHEMA, JsonSchemaType.NUMBER, JsonSchemaType.STRING)
                .getErrorMessage();
        assertEquals("#: expected type: String, found: Integer", actual);
    }
}
