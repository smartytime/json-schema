package io.sbsp.jsonschema.six;

import io.sbsp.jsonschema.impl.JsonSchemaImpl;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class JsonSchemaTest {

    @Test
    public void testEquals() {
        EqualsVerifier.forClass(JsonSchemaImpl.class)
                .withOnlyTheseFields("keywords")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();

    }

}