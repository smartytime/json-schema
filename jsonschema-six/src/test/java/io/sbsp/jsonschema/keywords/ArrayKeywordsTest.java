package io.sbsp.jsonschema.keywords;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class ArrayKeywordsTest {
    @Test
    public void testArrayKeywordsEquals() {
        EqualsVerifier.forClass(ArrayKeywords.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void testArrayKeywordsBuilderEquals() {
        EqualsVerifier.forClass(ArrayKeywords.ArrayKeywordsBuilder.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}