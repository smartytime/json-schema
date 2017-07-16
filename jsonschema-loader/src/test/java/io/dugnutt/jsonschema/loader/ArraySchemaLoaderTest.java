package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.NullSchema;
import org.junit.Assert;
import org.junit.Test;
import io.dugnutt.jsonschema.six.SchemaException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class ArraySchemaLoaderTest extends BaseLoaderTest {

    public ArraySchemaLoaderTest() {
        super("arraytestschemas.json");
    }

    @Test
    public void additionalItemSchema() {
        assertTrue(getJsonObjectForKey("additionalItemSchema") instanceof ArraySchema);
    }

    @Test
    public void arrayByAdditionalItems() {
        ArraySchema actual = (ArraySchema) getJsonObjectForKey("arrayByAdditionalItems");
        assertFalse(actual.isRequiresArray());
    }

    @Test
    public void arrayByItems() {
        ArraySchema actual = (ArraySchema) getJsonObjectForKey("arrayByItems");
        assertNotNull(actual);
    }

    @Test
    public void arraySchema() {
        ArraySchema actual = (ArraySchema) getJsonObjectForKey("arraySchema");
        assertNotNull(actual);
        assertEquals(2, actual.getMinItems().intValue());
        assertEquals(3, actual.getMaxItems().intValue());
        assertTrue(actual.isPermitsAdditionalItems());
        Assert.assertEquals(NullSchema.INSTANCE, actual.getAllItemSchema());
    }

    @Test(expected = SchemaException.class)
    public void invalidAdditionalItems() {
        getJsonObjectForKey("invalidAdditionalItems");
    }

    @Test(expected = SchemaException.class)
    public void invalidArrayItemSchema() {
        getJsonObjectForKey("invalidArrayItemSchema");
    }

    @Test(expected = SchemaException.class)
    public void invalidItemsArraySchema() {
        getJsonObjectForKey("invalidItemsArraySchema");
    }
}
