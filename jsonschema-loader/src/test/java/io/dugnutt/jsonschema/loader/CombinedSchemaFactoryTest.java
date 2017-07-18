package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.MultipleTypeSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StringSchema;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class CombinedSchemaFactoryTest extends BaseLoaderTest {

    public CombinedSchemaFactoryTest() {
        super("combinedtestschemas.json");
    }

    @Test
    public void combinedSchemaLoading() {
        Schema actual = getSchemaForKey("combinedSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void combinedSchemaWithBaseSchema() {
        Schema actual = getSchemaForKey("combinedSchemaWithBaseSchema");
        assertEquals(2, actual.getAnyOfSchema().getSubSchemas().stream()
                .filter(schema -> schema instanceof StringSchema).count());
    }

    @Test
    public void combinedSchemaWithExplicitBaseSchema() {
        Schema actual = getSchemaForKey("combinedSchemaWithExplicitBaseSchema");
        assertEquals(2, actual.getAnyOfSchema().getSubSchemas().stream()
                .filter(schema -> schema instanceof StringSchema).count());
    }

    @Test
    public void combinedSchemaWithMultipleBaseSchemas() {
        Schema actual = getSchemaForKey("combinedSchemaWithMultipleBaseSchemas");
        assertTrue(actual instanceof MultipleTypeSchema);
    }
}
