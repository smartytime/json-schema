package io.sbsp.jsonschema.keywords;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class NumberKeywordsTest {

    @Test
    public void testNumberKeywordsEquals() {
        EqualsVerifier.forClass(NumberKeywords.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void testNumberKeywordsBuilderEquals() {
        EqualsVerifier.forClass(NumberKeywords.NumberKeywordsBuilder.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}