package io.sbsp.jsonschema.six;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class JsonSchemaTest {

    @Test
    public void testEquals() {
        EqualsVerifier.forClass(JsonSchema.class)
                .withIgnoredFields("location")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();

    }

}