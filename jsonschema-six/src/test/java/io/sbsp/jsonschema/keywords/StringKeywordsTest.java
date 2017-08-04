package io.sbsp.jsonschema.keywords;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class StringKeywordsTest {

    @Test
    public void testStringKeywordsEquals() {
        EqualsVerifier.forClass(StringKeywords.class)
                .verify();
    }

    @Test
    public void testStringKeywordsBuilderEquals() {
        EqualsVerifier.forClass(StringKeywords.StringKeywordsBuilder.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

}