package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.NullSchema;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;

import static io.dugnutt.jsonschema.loader.LoadingTestSupport.failWith;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class ArraySchemaFactoryTest extends BaseLoaderTest {

    public ArraySchemaFactoryTest() {
        super("arraytestschemas.json");
    }

    @Test
    public void additionalItemSchema() {
        assertTrue(getSchemaForKey("additionalItemSchema") instanceof ArraySchema);
    }

    @Test
    public void arrayByAdditionalItems() {
        ArraySchema actual = (ArraySchema) getSchemaForKey("arrayByAdditionalItems");
        assertFalse(actual.isRequiresArray());
    }

    @Test
    public void arrayByItems() {
        ArraySchema actual = (ArraySchema) getSchemaForKey("arrayByItems");
        assertNotNull(actual);
    }

    @Test
    public void arraySchema() {
        ArraySchema actual = (ArraySchema) getSchemaForKey("arraySchema");
        assertNotNull(actual);
        assertEquals(2, actual.getMinItems().intValue());
        assertEquals(3, actual.getMaxItems().intValue());
        Assert.assertEquals(NullSchema.NULL_SCHEMA, actual.getAllItemSchema());
    }

    @Test
    public void invalidAdditionalItems() {
        failWith(UnexpectedValueException.class)
                .input(getJsonObjectForKey("invalidAdditionalItems"))
                .expectedSchemaLocation("#/additionalItems")
                .expect();
    }

    @Test
    public void invalidArrayItemSchema() {
        failWith(UnexpectedValueException.class)
                .input(getJsonObjectForKey("invalidArrayItemSchema"))
                .expectedSchemaLocation("#/items/0")
                .expected(err->{
                    System.out.println();
                })
                .expect();
    }

    @Test
    public void invalidItemsArraySchema() {
        final JsonObject schemaJson = getJsonObjectForKey("invalidItemsArraySchema");
        failWith(UnexpectedValueException.class)
                .input(schemaJson)
                .expectedSchemaLocation("#/items")
                .expect();
    }
}
