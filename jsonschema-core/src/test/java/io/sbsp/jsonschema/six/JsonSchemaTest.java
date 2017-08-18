package io.sbsp.jsonschema.six;

import io.sbsp.jsonschema.impl.JsonSchemaImpl;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class JsonSchemaTest {

    @Test
    public void testEquals() {
        EqualsVerifier.forClass(JsonSchemaImpl.class)
                .withPrefabValues(KeywordInfo.class, Keywords.$ID, Keywords.$SCHEMA)
                .withOnlyTheseFields("keywords")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();

    }

}