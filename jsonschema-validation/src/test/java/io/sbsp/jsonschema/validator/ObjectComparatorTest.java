package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.ObjectComparator;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonNumber;

public class ObjectComparatorTest {

    @Test
    public void testLexicalEquivalentForNumbers() {
        JsonNumber testNumA = JsonUtils.readValue("1.00", JsonNumber.class);
        JsonNumber testNumB = JsonUtils.readValue("1.0", JsonNumber.class);
        JsonNumber testNumC = JsonUtils.readValue("1", JsonNumber.class);

        Assert.assertFalse("1.00 not equiv as 1.0", ObjectComparator.lexicalEquivalent(testNumA, testNumB));
        Assert.assertFalse("1.00 not equiv as 1", ObjectComparator.lexicalEquivalent(testNumA, testNumC));
        Assert.assertFalse("1.0 not equiv as 1", ObjectComparator.lexicalEquivalent(testNumB, testNumC));
    }

}