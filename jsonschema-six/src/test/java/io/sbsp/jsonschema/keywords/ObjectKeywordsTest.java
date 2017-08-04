package io.sbsp.jsonschema.keywords;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class ObjectKeywordsTest {
    @Test
    public void testObjectKeywordsEquals() {
        EqualsVerifier.forClass(ObjectKeywords.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void testObjectKeywordsBuilderEquals() {
        EqualsVerifier.forClass(ObjectKeywords.ObjectKeywordsBuilder.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}