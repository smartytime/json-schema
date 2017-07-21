package io.dugnutt.jsonschema.six;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static io.dugnutt.jsonschema.six.JsonSchemaType.*;
import static io.dugnutt.jsonschema.six.SchemaLocation.schemaLocation;

import static io.dugnutt.jsonschema.six.TestErrorHelper.failure;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class SchemaExceptionTest {

    private static final NullSchema SCHEMA = NullSchema.builder()
            .build();

    @Rule
    public final ExpectedException expExc = ExpectedException.none();

    @Test
    public void nullActual() {
        NullSchema schema = NullSchema.builder().location(schemaLocation("#/required/2")).build();
        String actual = failure(schema, STRING, NULL).getMessage();
        assertEquals("#/required/2: expected type: string, found: null", actual);
    }

    @Test
    public void nullJSONPointer() {
        expExc.expect(NullPointerException.class);
        expExc.expectMessage("schema must not be null");
        failure(null, NUMBER, STRING);
    }

    @Test
    public void nullWithMessage() {
        NullSchema schema = NullSchema.builder().location(schemaLocation("#/required/2")).build();
        String actual = failure(schema, STRING, NULL).getMessage();
        assertEquals("#/required/2: expected type: string, found: null", actual);
    }

    @Test
    public void testBuildMessageSingleExcType() {
        String actual = failure(SCHEMA, NUMBER, STRING)
                .getErrorMessage();
        assertEquals("expected type: number, found: string", actual);
    }
}
