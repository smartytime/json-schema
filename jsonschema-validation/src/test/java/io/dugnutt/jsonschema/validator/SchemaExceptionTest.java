package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.Schema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static io.dugnutt.jsonschema.six.JsonSchemaType.NULL;
import static io.dugnutt.jsonschema.six.JsonSchemaType.NUMBER;
import static io.dugnutt.jsonschema.six.JsonSchemaType.STRING;
import static io.dugnutt.jsonschema.validator.TestErrorHelper.failure;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class SchemaExceptionTest {

    private static final Schema NULL_SCHEMA = Schema.jsonSchemaBuilder().type(NULL).build();

    @Rule
    public final ExpectedException expExc = ExpectedException.none();

    @Test
    public void nullJSONPointer() {
        expExc.expect(NullPointerException.class);
        expExc.expectMessage("schema must not be null");
        failure(null, NUMBER, STRING);
    }

    @Test
    public void nullWithMessage() {
        Schema schema = Schema.jsonSchemaBuilderWithId("#/required/2").type(NULL).build();
        String actual = failure(schema, STRING, NULL).getMessage();
        assertEquals("#/required/2: expected type: string, found: null", actual);
    }

    @Test
    public void testBuildMessageSingleExcType() {
        String actual = failure(NULL_SCHEMA, NUMBER, STRING)
                .getErrorMessage();
        assertEquals("expected type: number, found: string", actual);
    }
}
